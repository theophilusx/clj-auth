(ns theophilusx.auth.actions
  (:require [theophilusx.auth.log :as log]
            [theophilusx.auth.db :as db]
            [theophilusx.auth.mail :as mail]
            [theophilusx.auth.utils :as utils]
            [theophilusx.auth.core :refer [config]]))

(defn create-request-record
  "Create a new verification record for give email address and insert it into db."
  [user-id req-type]
  (try 
    (let [vid (str (random-uuid))]
      (db/add-request-record user-id vid req-type))
    (catch Exception e
      (let [msg (str "create-request-record; Failed to add " req-type
                     " for user " user-id)]
        (log/error msg e)
        (throw (ex-info msg
                        {:use   user-id
                         :tyupe req-type} e))))))

(defn request-confirm-id
  "Generate new verification record and send email request to verify."
  [user]
  (try 
    (let [{:keys [email user_id]} (db/get-user user)]
      (when (nil? email)
        (throw (ex-info (str "No user ID found for " user)
                        {:user user})))
      (let [{:keys [req_key]}    (create-request-record user_id :confirm)
            {:keys [domain-name web-protocol
                    api-prefix]} (:theophilusx.auth.routes/site @config)
            link                 (str web-protocol domain-name api-prefix
                                      "/confirm/" user_id "/" req_key)
            rs                   (mail/send-confirm-msg email link)]
        (when (not= :success rs)
          (throw (ex-info (str "Failed to send confirm id to " email)
                          {:mail-result rs})))
        {:mail-status rs
         :email       email
         :user_id     user_id
         :req_key     req_key}))
    (catch Exception e
      (let [msg (str "request-confirm-id: Failed to generate verify request for user " user)]
        (log/error msg e)
        (throw (ex-info msg
                        {:user user} e))))))

(defn create-id
  "Create a new user."
  [email first-name last-name password]
  (try 
    (let [pwd               (utils/hash-pwd password)
          {:keys [user_id]} (db/add-user email first-name last-name pwd)
          rs                (request-confirm-id user_id)]
      (when (not= :success (:mail-status rs))
        (throw (ex-info (str "Request confirm ID failed for " email)
                        {:result rs})))
      rs)
    (catch Exception e
      (let [msg (str "create-id: Failed to create ID for " email)]
        (log/error msg e)
        (throw (ex-info msg
                        {:email      email
                         :first-name first-name
                         :last-name  last-name
                         :password   (str "Password has " (count password)
                                          " characters")} e))))))

(defn verify-id
  "Mark the account linked with supplied verificaiton ID as verified."
  [user-id key ip]
  (try 
    (db/complete-request-record user-id key ip)
    (db/set-user-status :confirm user-id)
    (catch Exception e
      (let [msg (str "verify-id: Failed to mark ID " user-id " as confirmed")]
        (log/error msg e)
        (throw (ex-info msg
                        {:user user-id
                         :key  key
                         :ip   ip} e))))))

(comment
  (let [{:keys [email user_id] :as rs} (db/get-user "john@example.com")]
    (println (str "Email: " email " User ID: " user_id " rs: " rs))) 
  )
