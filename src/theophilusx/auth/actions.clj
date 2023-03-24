(ns theophilusx.auth.actions
  (:require [buddy.hashers :as hashers]
            [taoensso.timbre :as log]
            [taoensso.timbre :refer [debug info]]
            [theophilusx.auth.db :as db]))

(defn register-user
  "Register a new user."
  [email first-name last-name password]
  (let [user (db/get-user email)]
    (cond
      (and (= :ok (:db-status user))
           (nil? (:result user))) (let [pwd (hashers/derive (or password (random-uuid)) {:alg :bcrypt+blake2b-512})
                                        rslt (db/add-user email first-name last-name pwd)]
                                    (info (str "register-user: Added " email))
                                    rslt)
      (= :error (:db-status user)) (do
                                     (info (str "register-user: Failed to registger " email " " user))
                                     user)
      (not (nil? (:result user))) (do
                                    (info (str "register-user: Failed to register "
                                               email ". Already registered"))
                                    {:db-status :exists
                                     :error-msg (str "Identity " email " already registered")})
      :else (do
              (info (str "register-user: Unknown failure registering " email
                         " " user))
              {:db-status :error
               :error-msg "Unexpected failure"}))))
