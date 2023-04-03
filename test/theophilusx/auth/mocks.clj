(ns theophilusx.auth.mocks
  (:require [taoensso.timbre :as log]))


(def pwd
  "Secret password for testing = 'my secret'"
  "bcrypt+blake2b-512$69e00d2a1775aff71fd3c384bdf4831a$12$099ade052f6870b31b6d3b90e8452ae5c66371da329c85e5")

(def user-data {"fred@example.com" {:email "fred@example.com"
                                    :first_name "Fred"
                                    :last_name "Flintstone"
                                    :password pwd
                                    :id_status "verify"
                                    :created_by "system"
                                    :created_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                    :modified_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                    :modified_by "system"}
                "barney@example.com" {:email "barney@example.com"
                                      :first_name "Barney"
                                      :last_name "Rubble"
                                      :password pwd
                                      :id_status "recover"
                                      :created_by "system"
                                      :created_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                      :modified_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                      :modified_by "system"}
                "Wilma@example.com" {:email "wilma@exmaple.com"
                                     :first_name "Wilma"
                                     :last_name "Flintstone"
                                     :password pwd
                                     :id_status "locked"
                                     :created_by "system"
                                     :created_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                     :modified_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                     :modified_by "system"}
                "betty@example.com" {:email "betty@exmaple.com"
                                     :first_name "Betty"
                                     :last_name "Rubble"
                                     :password pwd
                                     :id_status "contact"
                                     :created_by "system"
                                     :created_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                     :modified_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                     :modified_by "system"}
                "pebbles@example.com" {:email "pebbles@exmaple.com"
                                       :first_name "Pebbles"
                                       :last_name "Flintstone"
                                       :password pwd
                                       :id_status "ok"
                                       :created_by "system"
                                       :created_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                       :modified_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                       :modified_by "system"}
                "bammbamm@exmaple.com" {:email "bammbamm@exmaple.com"
                                        :first_name "Bamm-Bamm"
                                        :last_name "Rubble"
                                        :password pwd
                                        :id_status "ok"
                                        :created_by "system"
                                        :created_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                        :modified_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                        :modified_by "system"}
                "sam@example.com" {:email "sam@exmaple.com"
                                   :first_name "Sam"
                                   :last_name "Slate"
                                   :password pwd
                                   :id_status "ok"
                                   :created_by "system"
                                   :created_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                   :modified_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                   :modified_by "system"}
                "arnold@example.com" {:email "arnold@exmaple.com"
                                      :first_name "Arnold"
                                      :last_name "Mudroch"
                                      :password pwd
                                      :id_status "ok"
                                      :created_by "system"
                                      :created_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                      :modified_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                      :modified_by "system"}
                "joe@exmaple.com" {:email "joe@exmaple.com"
                                   :first_name "Joe"
                                   :last_name "Rockhead"
                                   :password pwd
                                   :id_status "ok"
                                   :created_by "system"
                                   :created_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                   :modified_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                   :modified_by "system"}
                "roxy@exmaple.com" {:email "roxy@exmaple.com"
                                    :first_name "Roxy"
                                    :last_name "Rubble"
                                    :password pwd
                                    :id_status "ok"
                                    :created_by "system"
                                    :created_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                    :modified_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                    :modified_by "system"}
                "stoney@example.com" {:email "stoney@exmaple.com"
                                      :first_name "Stoney"
                                      :last_name "Curtis"
                                      :password pwd
                                      :id_status "ok"
                                      :created_by "system"
                                      :created_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                      :modified_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                      :modified_by "system"}
                "perry@example.com" {:email "perry@exmaple.com"
                                     :first_name "Perry"
                                     :last_name "Masonary"
                                     :password pwd
                                     :id_status "ok"
                                     :created_by "system"
                                     :created_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                     :modified_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                     :modified_by "system"}
                "pearl@example.com" {:email "pearl@exmaple.com"
                                     :first_name "Pearl"
                                     :last_name "Slaghoople"
                                     :password pwd
                                     :id_status "ok"
                                     :created_by "system"
                                     :created_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                     :modified_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                     :modified_by "system"}
                "chip@example.com"  {:email "chip@exmaple.com"
                                     :first_name "Chip"
                                     :last_name "Rubble"
                                     :password pwd
                                     :id_status "ok"
                                     :created_by "system"
                                     :created_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                     :modified_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                                     :modified_by "system"}})

(defn get-id [email]
  (log/debug "mocks/get-id" email)
  (if (= "bad@example.com" email)
    {:db-status :error
     :error-msg "Something bad this way comes!"
     :result nil}
    {:db-status :ok
     :result (get user-data email nil)}))

(defn add-id [email first-name last-name password]
  (log/debug "mocks/add-id " email first-name last-name password)
  (cond
    (= "bad@example.com" email) {:db-status :error
                                 :error-code "0"
                                 :error-state "22000"
                                 :error-msg "Something bad this way comes!"
                                 :result nil}
    (contains? user-data email) {:db-status :error
                                 :error-code "0"
                                 :error-state "23505"
                                 :error-message "ERROR: Unique key violation"
                                 :result nil}
    :else {:db-status :ok
           :result {:email email
                    :first_name first-name
                    :last_name last-name
                    :password password
                    :created_by "system"
                    :modified_by "system"
                    :created_dt #inst "2023-04-01T02:13:55.945266000-00:00"
                    :modified_dt #inst "2023-04-01T02:13:55.945266000-00:00"}}))
