(ns decent-reddit-receiver.jraw
  (:require [environ.core :refer [env]])
  (:import net.dean.jraw.http.UserAgent
           net.dean.jraw.RedditClient
           net.dean.jraw.http.oauth.Credentials
           net.dean.jraw.http.AuthenticationMethod))

(def agent      (env :decent-agent))
(def app        (env :decent-app))
(def version    (env :decent-version))
(def user       (env :decent-user))
(def password   (env :decent-password))
(def app-id     (env :decent-app-id))
(def app-secret (env :decent-app-secret))




(defn get-reddit-client
  "Takes a client-spec returns a built reddit-client.

   client-spec:

   :agent
   :app
   :version
   :user
   :password
   :app-id
   :app-secret"
  [client-spec]
  (let [{:keys [agent app version user password app-id app-secret]} client-spec
        user-agent  (UserAgent/of agent app version user)
        client      (RedditClient. user-agent)
        credentials (Credentials/script user password app-id app-secret)
        oauth-data  (.. client getOAuthHelper (easyAuth credentials))
        _           (.authenticate client oauth-data)]
    client))
