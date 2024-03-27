(ns theophilusx.auth.db-test
  (:require [clojure.test :refer :all]
            [theophilusx.auth.db :as sut]))

(deftest get-user
  (testing "Get known user using email address"
    (let [email "john@example.com"
          rslt  (sut/get-user email)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (some? (:result rslt)))
      (is (contains? (:result rslt) :user_id))
      (is (number? (get-in rslt [:result :user_id])))
      (is (= 1 (get-in rslt [:result :user_id])))
      (is (contains? (:result rslt) :email))
      (is (string? (get-in rslt [:result :email])))
      (is (= email (get-in rslt [:result :email])))
      (is (contains? (:result rslt) :first_name))
      (is (string? (get-in rslt [:result :first_name])))
      (is (= "John" (get-in rslt [:result :first_name])))
      (is (contains? (:result rslt) :last_name))
      (is (string? (get-in rslt [:result :last_name])))
      (is (= "Smith" (get-in rslt [:result :last_name])))))
  (testing "Get known user using user ID"
    (let [id   1
          rslt (sut/get-user id)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (some? (:result rslt)))
      (is (contains? (:result rslt) :user_id))
      (is (number? (get-in rslt [:result :user_id])))
      (is (= id (get-in rslt [:result :user_id])))
      (is (contains? (:result rslt) :email))
      (is (string? (get-in rslt [:result :email])))
      (is (= "john@example.com" (get-in rslt [:result :email])))
      (is (contains? (:result rslt) :first_name))
      (is (string? (get-in rslt [:result :first_name])))
      (is (= "John" (get-in rslt [:result :first_name])))
      (is (contains? (:result rslt) :last_name))
      (is (string? (get-in rslt [:result :last_name])))
      (is (= "Smith" (get-in rslt [:result :last_name])))))
  (testing "Get unknown user using email"
    (let [rslt (sut/get-user "unknown@exmaple.com")]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :error (:status rslt)))
      (is (nil? (:result rslt)))
      (is (some? (:error-code rslt)))))
  (testing "Get unknown user using ID"
    (let [rslt (sut/get-user 0)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :error (:status rslt)))
      (is (nil? (:result rslt)))
      (is (some? (:error-code rslt))))))

(deftest add-user
  (testing "Add new user"
    (let [rslt (sut/add-user "fred@example.com" "Fred" "Flintstone" "Fred's secret")]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (some? (:result rslt)))
      (is (contains? (:result rslt) :user_id))
      (is (number? (get-in rslt [:result :user_id])))
      (is (pos? (get-in rslt [:result :user_id])))
      (is (contains? (:result rslt) :email))
      (is (= "fred@example.com" (get-in rslt [:result :email])))
      (is (contains? (:result rslt) :first_name))
      (is (= "Fred" (get-in rslt [:result :first_name])))
      (is (contains? (:result rslt) :last_name))
      (is (= "Flintstone" (get-in rslt [:result :last_name])))
      (is (contains? (:result rslt) :password))
      (is (= "Fred's secret" (get-in rslt [:result :password])))))
  (testing "Add conflicting ID"
    (let [rslt (sut/add-user "fred@example.com" "Fred2" "Flintstone" "Fred2's secret")]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :error-code))
      (is (contains? rslt :result))
      (is (= :error (:status rslt)))
      (is (nil? (:result rslt)))
      (is (some? (:error-code rslt)))
      (is (= :error (:status rslt)))
      (is (= "23505" (:error-code rslt))))))

(deftest set-user-status
  (testing "Set user status with email to confirmed."
    (let [email "john@example.com"
          rslt  (sut/set-user-status :confirmed email)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (map? (:result rslt)))
      (is (= "confirmed" (get-in rslt [:result :id_status])))
      (is (= email (get-in rslt [:result :email])))))
  (testing "Set user status with email to locked"
    (let [email "jane@example.com"
          rslt  (sut/set-user-status :locked email)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (map? (:result rslt)))
      (is (= "locked" (get-in rslt [:result :id_status])))
      (is (= email (get-in rslt [:result :email])))))
  (testing "Set user status with email to contact"
    (let [email "bobby@example.com"
          rslt  (sut/set-user-status :contact email)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (map? (:result rslt)))
      (is (= "contact" (get-in rslt [:result :id_status])))
      (is (= email (get-in rslt [:result :email])))))
  (testing "Setting status using email for non-existent user returns nil"
    (let [email "not-exist@nowhere.com"
          rslt  (sut/set-user-status :archive email)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :error (:status rslt)))
      (is (nil? (:result rslt)))))
  (testing "Set user status using ID to confirmed."
    (let [id   1
          rslt (sut/set-user-status :confirmed id)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (map? (:result rslt)))
      (is (= "confirmed" (get-in rslt [:result :id_status])))
      (is (= id (get-in rslt [:result :user_id])))))
  (testing "Set user status using ID to locked"
    (let [id   2
          rslt (sut/set-user-status :locked id)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (map? (:result rslt)))
      (is (= "locked" (get-in rslt [:result :id_status])))
      (is (= id (get-in rslt [:result :user_id])))))
  (testing "Set user status using ID to contact"
    (let [id   3
          rslt (sut/set-user-status :contact id)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (map? (:result rslt)))
      (is (= "contact" (get-in rslt [:result :id_status])))
      (is (= id (get-in rslt [:result :user_id])))))
  (testing "Setting non-existent user status using ID returns nil"
    (let [id   0
          rslt (sut/set-user-status :archive id)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (nil? (:result rslt))))))

(deftest delete-user
  (testing "Delete existing user providing email."
    (let [rslt (sut/delete-user "fred@example.com")]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (some? (:result rslt)))
      (is (contains? (:result rslt) :email))
      (is (= "fred@example.com" (get-in rslt [:result :email])))))
  (testing "Delete non-existing user providing email."
    (let [rslt (sut/delete-user-with-email "fred@example.com")]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (nil? (:result rslt)))))
  (testing "Delete existing user with provided user id."
    (let [user-id (get-in (sut/add-user "pdutton@voldamort.com"
                                        "Peter" "Dutton"
                                        "I'm a wanker")
                          [:result :user_id])
          rslt    (sut/delete-user user-id)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (some? (:result rslt)))
      (is (contains? (:result rslt) :email))
      (is (= "pdutton@voldamort.com" (get-in rslt [:result :email])))))
  (testing "Delete non-existing user given user id."
    (let [rslt (sut/delete-user 0)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (nil? (:result rslt))))))

(deftest add-request-record
  (testing "Add confirm request record for user."
    (let [key  (str (random-uuid))
          rslt (sut/add-request-record 1 key "confirm")]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (map? (:result rslt)))
      (is (= 1 (get-in rslt [:result :user_id])))
      (is (= key (get-in rslt [:result :req_key])))
      (is (= "confirm" (get-in rslt [:result :req_type])))))
  (testing "Add recover request record for user."
    (let [key  (str (random-uuid))
          rslt (sut/add-request-record 2 key "recover")]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (map? (:result rslt)))
      (is (= 2 (get-in rslt [:result :user_id])))
      (is (= key (get-in rslt [:result :req_key])))
      (is (= "recover" (get-in rslt [:result :req_type])))))
  (testing "Add recover request record for user."
    (let [key  (str (random-uuid))
          rslt (sut/add-request-record 3 key "archive")]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (map? (:result rslt)))
      (is (= 3 (get-in rslt [:result :user_id])))
      (is (= key (get-in rslt [:result :req_key])))
      (is (= "archive" (get-in rslt [:result :req_type]))))))

(deftest get-request-record
  (testing "Retrieve request record given user ID and request key"
    (let [user-id 1
          key     (str (random-uuid))
          rslt1   (sut/add-request-record user-id key "confirm")]
      (is (= :ok (:status rslt1)))
      (when (= :ok (:status rslt1))
        (let [rslt2 (sut/get-request-record user-id key)]
          (is (map? rslt2))
          (is (contains? rslt2 :status))
          (is (contains? rslt2 :result))
          (is (= :ok (:status rslt2)))
          (is (map? (:result rslt2)))
          (is (= user-id (get-in rslt2 [:result :user_id])))
          (is (= key (get-in rslt2 [:result :req_key])))
          (is (= "confirm" (get-in rslt2 [:result :req_type]))))))))

(deftest get-open-user-requests
  (testing "Successfully returns list of open requests for user ID"
    (let [rslt (sut/get-open-user-requests 1)]
      (is (map? rslt))
      (is (= :ok (:status rslt)))
      (is (:result rslt))
      (is (pos? (count (:result rslt))))))
  (testing "Returns empty result for non-existing user ID requests"
    (let [rslt (sut/get-open-user-requests -1)]
      (is (map? rslt))
      (is (= :ok (:status rslt)))
      (is (zero? (count (:result rslt)))))))

(deftest complete-request-record
  (testing "Complete open request record"
    (let [{:keys [user_id req_key]} (first (:result (sut/get-open-user-requests 1)))
          rslt                      (sut/complete-request-record user_id req_key)]
      (is (map? rslt))
      (is (= :ok (:status rslt)))
      (is (= user_id (:user_id (:result rslt))))
      (is (= req_key (:req_key (:result rslt))))
      (is (:completed (:result rslt)))))
  (testing "Completing non-existent record returns empty list"
    (let [rslt (sut/complete-request-record -1 "bad-key")]
      (println (str "Result: " rslt))
      (is (map? rslt))
      (is (= :error (:status rslt)))
      (is (nil? (:result rslt))))))

(deftest get-message
  (testing "Get verify message"
    (let [rslt (sut/get-message :verify)]
      (is (map? rslt))
      (is (some? (:result rslt)))
      (is (= :ok (:status rslt)))
      (is (contains? (:result rslt) :message))))
  (testing "Get recoverjmessage"
    (let [rslt (sut/get-message :recover)]
      (is (map? rslt))
      (is (some? (:result rslt)))
      (is (= :ok (:status rslt)))
      (is (contains? (:result rslt) :message))))
  (testing "Get locked message"
    (let [rslt (sut/get-message :locked)]
      (is (map? rslt))
      (is (some? (:result rslt)))
      (is (= :ok (:status rslt)))
      (is (contains? (:result rslt) :message))))
  (testing "Get contact message"
    (let [rslt (sut/get-message :contact)]
      (is (map? rslt))
      (is (some? (:result rslt)))
      (is (= :ok (:status rslt)))
      (is (contains? (:result rslt) :message))))
  (testing "Get non-existent message handled gracefully" 
    (let [rslt (sut/get-message :no-such-message)]
      (is (map? rslt))
      (is (= :ok (:status rslt)))
      (is (nil? (:result rslt)))))) 

(defn clear-ids []
  (sut/delete-user-with-email "fred@example.com")
  (sut/truncate-table "auth.roles"))

(defn test-ns-hook []
  (clear-ids)
  (get-user)
  (add-user)
  (set-user-status)
  (delete-user)
  (add-request-record)
  (get-request-record)
  (get-open-user-requests)
  (complete-request-record)
  (get-message))  
