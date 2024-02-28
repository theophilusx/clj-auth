(ns theophilusx.auth.handlers
  (:require [taoensso.timbre :as log]
            [theophilusx.auth.utils :refer [map->str]]
            [theophilusx.auth.actions :refer [create-id verify-id]]
            [theophilusx.auth.db :refer [get-message]]))

(defn not-implemented [req]
  {:status 501
   :body (str "Not yet implemented\n"
              "Page " (:uri req) " functionality not yet implemented.\n"
              (select-keys req [:parameters :uri :request-method
                                :params :form-params :query-params
                                :path-params :body-params]))})

(defn authn [{{{:keys [email password]} :body} :parameters :as req}]
  (log/debug (str "authn: email: " email " password length: " (count password)))
  (log/debug "authn" req)
  {:status 200
   :body (merge {:title "Authn Request"
                 :desc "An authn post handler"
                 :request-keys (str "Keys: " (keys req))
                 :parameters (str (:parameters req))
                 :body-params (str (:body-params req))
                 :uri (:uri req)}
                (:body-params req))})

(defn create
  "Create a new user identity record."
  [{{{:keys [email first-name last-name password]} :body} :parameters :as req}]
  (log/debug (str "Keys: " (keys req)))
  (let [rslt (create-id email first-name last-name password)]
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
                          "email address " email)}}
        {:status 400
       :body {:msg (str "Failed to create new account. " (:error-msg rslt))}}))))

(defn confirm
  "Handler used to process new account confirmation requests."
  [{{{:keys [user-id key]} :path} :parameters :as req}]
  (log/debug "confirm: user-id = " user-id " key = " key)
  (log/debug "confirm-handler: Request keys = " (keys req))
  (let [rslt (verify-id user-id key (:remote-addr req))]
    (if (= :ok (:status rslt))
      {:status 200
       :body {:msg (str "Account " (get-in rslt [:result :email]) " has been verified!")}}
      {:status 400
       :body {:msg (str "Confirmation failed. " (:error-msg rslt))}})))

