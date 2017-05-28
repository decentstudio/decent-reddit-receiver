(ns decent-reddit-receiver.core
  (:require [clj-http.client :as http]
            [clojure.core.async :as a]
            [clojure.data.json :as json]
            [langohr [core :as rmq]
                     [channel :as lc]
                     [basic :as lb]])

  (:gen-class))

(def site "https://reddit.com")
(def dot-json ".json")

(defn get-daily-thread
  []
  (try
    (->> (http/get (str site "/r/ethtrader/hot" dot-json) {:as :json})
         :body
         :data
         :children
         (filter #(re-find #"\[ETH Daily Discussion\]" (-> % :data :title)))
         first)
    (catch clojure.lang.ExceptionInfo e nil)))

(defn get-daily-thread-every-x-seconds
  [x]
  (let [out-chan (a/chan)]
    (a/go-loop []
      (let [daily-thread (a/<! (a/thread (get-daily-thread)))]
        (when daily-thread
          (a/>! out-chan daily-thread))
        (a/<! (a/timeout (* x 1000)))
        (recur)))
    out-chan))

(defn publish-daily-thread
  [daily-in-chan publish-fn]
  (let [out-chan (a/chan)]
    (a/go-loop [daily-thread (a/<! daily-in-chan)]
      (when daily-thread
        (publish-fn "decent.reddit.submission" daily-thread)
        (a/>! out-chan daily-thread)
        (recur (a/<! daily-in-chan))))
    out-chan))

(defn get-initial-comments
  [daily-thread]
  (try
    (->> (http/get (str site (-> daily-thread :data :permalink) dot-json) {:as :json})
         :body
         second
         :data)
    (catch clojure.lang.ExceptionInfo e nil)))

(defn get-initial-comments-for-daily-thread
  [daily-in-chan]
  (let [out-chan (a/chan)]
    (a/go-loop [daily-thread (a/<! daily-in-chan)]
      (a/<! (a/timeout 1000))
      (when daily-thread
        (let [comments (a/<! (a/thread (get-initial-comments daily-thread)))]
          (when comments
            (a/>! out-chan comments)))
        (recur (a/<! daily-in-chan))))
    out-chan))

(defn publish-comments
  [initial-comments-in-chan publish-fn]
  (let [out-chan (a/chan)]
    (a/go-loop [initial-comments (a/<! initial-comments-in-chan)]
      (when initial-comments
        (->> initial-comments
             :children
             (filter #(re-matches #"t1" (:kind %)))
             (publish-fn "decent.reddit.comments.chunk"))
        (recur (a/<! initial-comments-in-chan))))
    out-chan))

(defn ->json
  [data]
  (json/write-str data :key-fn name))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [broker (rmq/connect)
        chan (lc/open broker)
        publish-fn (fn [topic payload] (lb/publish chan "amq.topic" topic (->json payload)))
        out (-> (get-daily-thread-every-x-seconds 25)
                (publish-daily-thread publish-fn)
                (get-initial-comments-for-daily-thread)
                (publish-comments publish-fn))
        _       (println "Running...")
        _       (a/<!! out)]))
