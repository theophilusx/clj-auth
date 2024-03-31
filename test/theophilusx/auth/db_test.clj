(ns theophilusx.auth.db-test
  (:require [clojure.test :refer :all]
            [theophilusx.auth.db :as sut]))

(deftest get-user
  (testing "Get known user using email address"
    (let [email "john@example.com"
          rslt  (sut/get-user email)]
      (is (map? rslt))
      (is (contains? rslt :user_id))
      (is (number? (:user_id rslt)))
      (is (= 1 (:user_id rslt)))
      (is (contains? rslt :email))
      (is (string? (:email rslt)))
      (is (= email (:email rslt)))
      (is (contains? rslt :first_name))
      (is (string? (:first_name rslt)))
      (is (= "John" (:first_name rslt)))
      (is (contains? rslt :last_name))
      (is (string? (:last_name rslt)))
      (is (= "Smith" (:last_name rslt)))))
  (testing "Get known user using user ID"
    (let [id   1
          rslt (sut/get-user id)]
      (is (map? rslt))
      (is (contains? rslt :user_id))
      (is (number? (:user_id rslt)))
      (is (= 1 (:user_id rslt)))
      (is (contains? rslt :email))
      (is (string? (:email rslt)))
      (is (= "john@example.com" (:email rslt)))
      (is (contains? rslt :first_name))
      (is (string? (:first_name rslt)))
      (is (= "John" (:first_name rslt)))
      (is (contains? rslt :last_name))
      (is (string? (:last_name rslt)))
      (is (= "Smith" (:last_name rslt)))))
  (testing "Get unknown user using email"
    (let [rslt (sut/get-user "unknown@exmaple.com")]
      (is (nil? rslt))))
  (testing "Get unknown user using ID"
    (let [rslt (sut/get-user 0)]
      (is (nil? rslt)))))

(deftest add-user
  (testing "Add new user"
    (let [rslt (sut/add-user "fred@example.com" "Fred" "Flintstone" "Fred's secret")]
      (is (map? rslt))
      (is (contains? rslt :user_id))
      (is (number? (:user_id rslt)))
      (is (pos? (:user_id rslt)))
      (is (contains? rslt :email))
      (is (= "fred@example.com" (:email rslt)))
      (is (contains? rslt :first_name))
      (is (= "Fred" (:first_name rslt)))
      (is (contains? rslt :last_name))
      (is (= "Flintstone" (:last_name rslt)))
      (is (contains? rslt :password))
      (is (= "Fred's secret" (:password rslt)))))
  (testing "Add conflicting ID"
    (is (thrown-with-msg? Exception #"add-user: Failed to add user"
                          (sut/add-user "fred@example.com" "Fred" "Flintstone" "Fred's secret")))))

(deftest set-user-status
  (testing "Set user status with email to confirmed."
    (let [email "john@example.com"
          rslt  (sut/set-user-status :confirmed email)]
      (is (map? rslt))
      (is (= email (:email rslt)))
      (is (= "confirmed" (:id_status rslt)))))
  (testing "Set user status with email to locked"
    (let [email "jane@example.com"
          rslt  (sut/set-user-status :locked email)]
      (is (map? rslt))
      (is (= email (:email rslt)))
      (is (= "locked" (:id_status rslt)))))
  (testing "Set user status with email to contact"
    (let [email "bobby@example.com"
          rslt  (sut/set-user-status :contact email)]
      (is (map? rslt))
      (is (= email (:email rslt)))
      (is (= "contact" (:id_status rslt)))))
  (testing "Setting status using email for non-existent user returns nil"
    (let [email "not-exist@nowhere.com"
          rslt  (sut/set-user-status :archive email)]
      (is (nil? rslt))))
  (testing "Set user status using ID to confirmed."
    (let [id   1
          rslt (sut/set-user-status :confirmed id)]
      (is (map? rslt))
      (is (= id (:user_id rslt)))
      (is (= "confirmed" (:id_status rslt)))))
  (testing "Set user status using ID to locked"
    (let [id   2
          rslt (sut/set-user-status :locked id)]
      (is (map? rslt))
      (is (= id (:user_id rslt)))
      (is (= "locked" (:id_status rslt)))))
  (testing "Set user status using ID to contact"
    (let [id   3
          rslt (sut/set-user-status :contact id)]
      (is (map? rslt))
      (is (= id (:user_id rslt)))
      (is (= "contact" (:id_status rslt)))))
  (testing "Setting non-existent user status using ID returns nil"
    (let [id   0
          rslt (sut/set-user-status :archive id)]
      (is (nil? rslt)))))

(deftest delete-user
  (testing "Delete existing user providing email."
    (let [rslt (sut/delete-user "fred@example.com")]
      (is (= 1 (count rslt)))
      (is (= "fred@example.com" (:email (first rslt))))))
  (testing "Delete non-existing user providing email."
    (let [rslt (sut/delete-user "fred@example.com")]
      (is (= 0 (count rslt)))))
  (testing "Delete existing user with provided user id."
    (let [user-id (:user_id (sut/add-user "pdutton@voldamort.com"
                                          "Peter" "Dutton"
                                          "I'm a wanker"))
          rslt    (sut/delete-user user-id)]
      (is (= 1 (count rslt)))
      (is (= "pdutton@voldamort.com" (:email (first rslt))))))
  (testing "Delete non-existing user given user id."
    (let [rslt (sut/delete-user 0)]
      (is (= 0 (count rslt))))))

(deftest add-request-record
  (testing "Add confirm request record for user."
    (let [key  (str (random-uuid))
          rslt (sut/add-request-record 1 key :confirm)]
      (is (map? rslt))
      (is (= 1 (:user_id rslt)))
      (is (= key (:req_key rslt)))
      (is (= "confirm" (:req_type rslt)))))
  (testing "Add recover request record for user."
    (let [key  (str (random-uuid))
          rslt (sut/add-request-record 2 key :recover)]
      (is (map? rslt))
      (is (= 2 (:user_id rslt)))
      (is (= key (:req_key rslt)))
      (is (= "recover" (:req_type rslt)))))
  (testing "Add recover request record for user."
    (let [key  (str (random-uuid))
          rslt (sut/add-request-record 3 key :archive)]
      (is (map? rslt))
      (is (= 3 (:user_id rslt)))
      (is (= key (:req_key rslt)))
      (is (= "archive" (:req_type rslt))))))

(deftest get-request-record
  (testing "Retrieve request record given user ID and request key"
    (let [user-id 1
          key     (str (random-uuid))
          rslt1   (sut/add-request-record user-id key "confirm")]
      (is (map? rslt1))
      (when (and (= user-id (:user_id rslt1))
                 (= key (:req_key rslt1)))
        (let [rslt2 (sut/get-request-record user-id key)]
          (is (map? rslt2))
          (is (= user-id (:user_id rslt2)))
          (is (= key (:req_key rslt2)))
          (is (= "confirm" (:req_type rslt2))))))))

(deftest get-open-user-requests
  (testing "Successfully returns list of open requests for user ID"
    (let [rslt (sut/get-open-user-requests 1)]
      (is (vector? rslt))
      (is (= 1 (:user_id (first rslt))))
      (is (false? (:completed (first rslt))))
      (is (= (count rslt) (count (filter #(false? (:completed %)) rslt))))))
  (testing "Returns empty result for non-existing user ID requests"
    (let [rslt (sut/get-open-user-requests -1)]
      (is (= 0 (count rslt))))))

(deftest complete-request-record
  (testing "Complete open request record"
    (let [{:keys [user_id req_key]} (first (sut/get-open-user-requests 1))
          rslt                      (sut/complete-request-record user_id req_key)]
      (is (map? rslt))
      (is (= user_id (:user_id rslt)))
      (is (= req_key (:req_key rslt)))
      (is (:completed rslt))))
  (testing "Completing non-existent record returns empty list"
    (let [rslt (sut/complete-request-record -1 "bad-key")]
      (println (str "Result: " rslt))
      (is (nil? rslt)))))

(deftest get-message
  (testing "Get verify message"
    (let [rslt (sut/get-message :verify)]
      (is (map? rslt))
      (is (contains? rslt :message))))
  (testing "Get recoverjmessage"
    (let [rslt (sut/get-message :recover)]
      (is (map? rslt))
      (is (contains? rslt :message))))
  (testing "Get locked message"
    (let [rslt (sut/get-message :locked)]
      (is (map? rslt))
      (is (contains? rslt :message))))
  (testing "Get contact message"
    (let [rslt (sut/get-message :contact)]
      (is (map? rslt))
      (is (contains? rslt :message))))
  (testing "Get non-existent message handled gracefully" 
    (let [rslt (sut/get-message :no-such-message)]
      (is (nil? rslt))))) 

(defn clear-ids []
  (sut/delete-user "fred@example.com")
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

