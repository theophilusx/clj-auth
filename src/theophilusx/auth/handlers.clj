(ns theophilusx.auth.handlers
  (:require [clojure.string :refer [join]]
            [taoensso.timbre :as log]
            [theophilusx.auth.utils :refer [map->str]]
            [theophilusx.auth.actions :refer [register-user]]))

(defn not-implemented-handler [req]
  {:status 501 :body (str "Not yet implemented\n"
                          "Page " (:uri req) " functionality not yet implemented.\n"
                          (map->str req))})

(defn authn-post-handler [req]
  {:status 200
   :body (merge {:title "Authn Request"
                 :desc "An authn post handler"
                 :request-keys (str "Keys: " (keys req))
                 :parameters (str (:parameters req))
                 :body-params (str (:body-params req))
                 :uri (:uri req)}
                (:body-params req))})

(defn register-handler
  "Create a new user identity record."
  [{:keys [body-params] :as req}]
  (let [rslt (register-user (:email body-params) (:first-name body-params)
                            (:last-name body-params) (:password body-params))]
    )
  )




