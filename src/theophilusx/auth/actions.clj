(ns theophilusx.auth.actions
  (:require [taoensso.timbre :as log]
            [theophilusx.auth.db :as db]
            [theophilusx.auth.mail :as mail]
            [theophilusx.auth.utils :as utils]
            [theophilusx.auth.core :refer [config]]))

(defn create-request-record
  "Create a new verification record for give email address and insert it into db."
  [user-id req-type]
  (let [vid (str (random-uuid))
        rslt (db/add-request-record user-id vid req-type)]
    (log/debug "create-request-record " rslt)
    (if (= :ok (:status rslt))
      {:status :ok
       :user-id user-id
       :vid vid
       :type req-type}
      rslt)))

(defn request-confirm-id
  "Generate new verification record and send emil request to verify."
  [email user-id]
  (let [rslt (create-request-record user-id "confirm")
        {:keys [domain-name web-protocol
                api-prefix]} (:theophilusx.auth.routes/site @config)]
    (if (= :ok (:status rslt))
      (let [link (str web-protocol domain-name api-prefix
                      "/confirm/" user-id "/" (:vid rslt))
            mail-rslt (mail/send-confirm-msg email link)]
        (when (not= :ok (:status mail-rslt))
          (log/error "request-confirm-id: Error sending verify request ", mail-rslt))
        mail-rslt)
      (do
        (log/error "request-confirm-id: Error generating verify record ", rslt)
        rslt))))

(defn create-id
  "Create a new user."
  [email first-name last-name password]
  (let [pwd (utils/hash-pwd password)
        id-rslt (db/add-user email first-name last-name pwd)]
    (log/debug "create-id: email: " email " result: " id-rslt)
    (if (= :ok (:status id-rslt))
      (let [rslt (request-confirm-id email (get-in id-rslt [:result :user_id]))]
        (log/debug "create-id: request-verify-id: " rslt)
        (if (= :ok (:status rslt))
          {:status :ok
           :result {:email email
                    :user-id (:user_id id-rslt)
                    :state :verify}}
          rslt))
      id-rslt)))

(defn verify-id
  "Mark the account linked with supplied verificaiton ID as verified."
  [user-id key ip]
  (log/debug (str "verify-id: user-id: " user-id " key: " key " ip: " ip))
  (let [rslt1 (db/complete-request-record user-id key ip)]
    (if (= :ok (:status rslt1))
      (let [rslt2 (db/set-user-status-with-id "confirmed" user-id)]
        (when (not= :ok (:status rslt2))
          (log/error "verify-id Result: " rslt2))
        rslt2)
      (do
        (log/error "verify-id Result: " rslt1)
        rslt1))))


(comment)


