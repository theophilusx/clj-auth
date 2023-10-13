(ns theophilusx.auth.actions
  (:require [taoensso.timbre :as log]
            [theophilusx.auth.db :as db]
            [theophilusx.auth.mail :as mail]
            [theophilusx.auth.utils :as utils]))

(defn create-verify-record
  "Create a new verification record for give email address and insert it into db."
  [email]
  (let [vid (random-uuid)
        rslt (db/add-confirm-record email vid)]
    (log/debug "create-verify-record " rslt)
    (if (= :ok (:status rslt))
      {:status :ok
       :email email
       :vid vid}
      rslt)))

(defn request-verify-id
  "Generate new verification record and send emil request to verify."
  [email]
  (let [rslt (create-verify-record email)]
    (if (= :ok (:status rslt))
      (let [link (str "http://localhost/confirm/" (.toString (:vid rslt)))
            mail-rslt (mail/send-confirm-msg email link)]
        (log/debug "request-verify-id " mail-rslt)
        mail-rslt)
      rslt)))

(defn create-id
  "Create a new user."
  [email first-name last-name password]
  (let [pwd (utils/hash-pwd password)
        id-rslt (db/add-id email first-name last-name pwd)]
    (log/debug "create-id: email: " email " result: " id-rslt)
    (if (= :ok (:status id-rslt))
      (let [rslt (request-verify-id email)]
        (log/debug "create-id: request-verify-id: " rslt)
        (if (= :ok (:status rslt))
          {:status :ok
           :result {:email email
                    :state :verify}}
          rslt))
      id-rslt)))

(comment)


