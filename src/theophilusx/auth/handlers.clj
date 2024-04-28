(ns theophilusx.auth.handlers
  (:require [theophilusx.auth.log :as log]
            [theophilusx.auth.utils :refer [map->str]]
            [theophilusx.auth.actions :refer [create-id verify-id]]
            [theophilusx.auth.db :refer [get-message]]
            [theophilusx.auth.template :refer [render-template]]))

(defn not-implemented [req]
  (let [v (select-keys req [:parameters :uri :request-method
                            :params :form-params :query-params
                            :path-params :body-params])]
    {:status 501
     :body (str (render-template "not-implemented.edn" v))}))

(defn authn [{{{:keys [email password]} :body} :parameters :as req}]
  (log/debug (str "authn: email: " email " password length: " (count password)))
  (log/debug "authn" req)
  {:status 200
   :body (str (render-template "home.edn"))})




