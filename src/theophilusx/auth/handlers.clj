(ns theophilusx.auth.handlers
  (:require [taoensso.timbre :as log]
            [theophilusx.auth.utils :refer [map->str]]
            [theophilusx.auth.actions :refer [create-id verify-id]]
            [theophilusx.auth.db :refer [get-message]]))

(defn not-implemented-handler [req]
  {:status 501
   :body (str "Not yet implemented\n"
              "Page " (:uri req) " functionality not yet implemented.\n"
              (map->str req))})

(defn authn-post-handler [req]
  (log/debug "authn-post-handler" req)
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
  (log/debug "register-handler: Request keys:" (keys req))
  (let [rslt (create-id (:email body-params)
                        (:first_name body-params)
                        (:last_name body-params)
                        (:password body-params))]
    (if (= :ok (:status rslt))
      (let [msg (get-message "verify")]
        (if (= :ok (:status msg))
          {:status 200
           :body {:msg (get-in msg [:result :message])}}
          {:status 500
           :body {:msg (str "Internal system error: " (:error-msg msg))}}))
      (if (= :db-unique-violation (:error-name rslt))
        {:status 400
         :body {:msg (str "There is already an existing account associted with the "
                          "email address " (:email body-params))}}
        {:status 400
       :body {:msg (str "Failed to create new account. " (:error-msg rslt))}}))))

(defn confirm-handler
  "Handler used to process new account confirmation requests."
  [{:keys [path-params remote-addr] :as req}]
  (log/debug "confirm-handler: Path params = " path-params)
  (log/debug "confirm-handler: Request keys = " (keys req))
  (let [rslt (verify-id (:id path-params) (:email path-params) remote-addr)]
    (if (= :ok (:status rslt))
      {:status 200
       :body {:msg (str "Account " (get-in rslt [:result :email]) " has been verified!")}}
      {:status 400
       :body {:msg (str "Confirmation failed. " (:error-msg rslt))}})))

