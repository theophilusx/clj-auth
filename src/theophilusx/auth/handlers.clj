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
  (let [rslt (register-user (:email body-params)
                            (:first_name body-params)
                            (:last_name body-params)
                            (:password body-params))]
    (cond
      (and (= :ok (:db-status rslt))
           (= "verify" (:id_status rslt))) {:status 200
                                            :body {:status "verify"
                                                   :msg (get-verify-message (:email rslt))}}
      (and (= :ok (:db-status rslt))
           (= "recover" (:id_status rslt))) {:status 200
                                             :body {:status "recover"
                                                    :msg (get-recover-message (:email rslt))}}
      (and (= :ok (:db-status rslt))
           (= "locked" (:id_status rslt))) {:status 200
                                            :body {:status "locked"
                                                   :msg "This account is temporarily locked. Pleas stry again later"}}
      (and (= :ok (:db-status rslt))
           (= "contact" (:id_status rslt))) {:status 200
                                             :body {:status "contact"
                                                    :msg "There is a problem with your account. Please contact support."}}
      (and (= :ok (:db-status rslt))
           (= "ok" (:id_status rslt)) {:status 200
                                       :body {:status "ok"
                                              :msg "This account is active and ready for use."}})
      (= :error (:db-status rslt)) {:status 503
                                    :body {:status "error"
                                           :msg "This service is not currently available. Please stry again later."}}
      {:status 500
       :body {:status "unexpected"
              :msg "An unexpected error has occurred. Please try again later."}})))




