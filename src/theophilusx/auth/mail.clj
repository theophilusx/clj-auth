(ns theophilusx.auth.mail
  (:require [postal.core :as post]
            [integrant.core :as ig]
            [hiccup.page :refer [html5]]
            [theophilusx.auth.utils :refer [read-env]]))

(def smtp-server (atom {}))

(def mail-environment (atom :dev))

(def smtp-from (atom nil))

(defmethod ig/init-key :theophilusx.auth.mail/post [_ config]
  (let [e (read-env "resources/.env.edn")
        svr {:host (:smtp-server config)
             :port (:smtp-port config)
             :tls (:smtp-tls config)
             :user (:smtp-user e)
             :pass (:smtp-password e)}]
    (when (= :prod (:env config))
      (reset! mail-environment :prod))
    (reset! smtp-from (:smtp-from e))
    (reset! smtp-server svr)))

(defn confirm-msg [link]
  {:body [:alternative
          {:type "text/plain"
           :content (str "\tVerify Account\n\t==============\n\n"
                         "Plase confirm your account by going to the below link\n\n\t"
                         link)}
          {:type "text/html"
           :content (html5 [:body
                            [:h1 "Verify Your Account"]
                            [:p "Please verify your account by going to the below link"]
                            [:a {:href link} link]])}]})
