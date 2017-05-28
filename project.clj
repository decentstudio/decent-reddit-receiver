(defproject decent-reddit-receiver "0.1.0-SNAPSHOT"
  :description "The Decent Reddit Receiver"
  :url "decentstudio.com"
  :license {:name "N/A"
            :url ""}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.443"]
                 [org.clojure/data.json "0.2.6"]
                 [com.novemberain/langohr "4.0.0"]
                 [clj-http "3.6.0"]
                 [environ "1.1.0"]
                 [net.dean.jraw/JRAW "0.9.0"]]

  :repositories [["bintray" "https://jcenter.bintray.com"]]
  :plugins [[lein-environ "1.1.0"]]
  :main ^:skip-aot decent-reddit-receiver.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
