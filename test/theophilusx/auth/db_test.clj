(ns theophilusx.auth.db-test
  (:require [clojure.test :refer [deftest is testing]]
            [theophilusx.auth.db :as sut]))

(deftest get-user-with-email
  (testing "Get known user with email"
    (let [email "john@example.com"
          rslt (sut/get-user-with-email email)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (not (nil? (:result rslt))))
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
  (testing "Get unknown ID"
    (let [rslt (sut/get-user-with-email "unknown@exmaple.com")]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :error (:status rslt)))
      (is (nil? (:result rslt)))
      (is (not (nil? (:error-code rslt)))))))

(deftest get-user-with-id
  (testing "Get known user with user id"
    (let [id 1
          rslt (sut/get-user-with-id id)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (not (nil? (:result rslt))))
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
  (testing "Get unknown ID"
    (let [rslt (sut/get-user-with-id 0)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :error (:status rslt)))
      (is (nil? (:result rslt)))
      (is (not (nil? (:error-code rslt)))))))

(deftest add-user
  (testing "Add new user"
    (let [rslt (sut/add-user "fred@example.com" "Fred" "Flintstone" "Fred's secret")]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (not (nil? (:result rslt))))
      (is (contains? (:result rslt) :user_id))
      (is (number? (get-in rslt [:result :user_id])))
      (is (<  0 (get-in rslt [:result :user_id])))
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
      (is (not (nil? (:error-code rslt))))
      (is (= :error (:status rslt)))
      (is (= "23505" (:error-code rslt))))))

(deftest set-user-status-with-email
  (testing "Set user status to confirmed."
    (let [email "john@example.com"
          rslt (sut/set-user-status-with-email :confirmed email)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (map? (:result rslt)))
      (is (= "confirmed" (get-in rslt [:result :id_status])))
      (is (= email (get-in rslt [:result :email])))))
  (testing "Set user status to locked"
    (let [email "jane@example.com"
          rslt (sut/set-user-status-with-email :locked email)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (map? (:result rslt)))
      (is (= "locked" (get-in rslt [:result :id_status])))
      (is (= email (get-in rslt [:result :email])))))
  (testing "Set user status to contact"
    (let [email "bobby@example.com"
          rslt (sut/set-user-status-with-email :contact email)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (map? (:result rslt)))
      (is (= "contact" (get-in rslt [:result :id_status])))
      (is (= email (get-in rslt [:result :email])))))
  (testing "Setting non-existent user status returns nil"
    (let [email "not-exist@nowhere.com"
          rslt (sut/set-user-status-with-email :archive email)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :error (:status rslt)))
      (is (nil? (:result rslt))))))

(deftest set-user-status-with-id
  (testing "Set user status to confirmed."
    (let [id 1
          rslt (sut/set-user-status-with-id :confirmed id)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (map? (:result rslt)))
      (is (= "confirmed" (get-in rslt [:result :id_status])))
      (is (= id (get-in rslt [:result :user_id])))))
  (testing "Set user status to locked"
    (let [id 2
          rslt (sut/set-user-status-with-id :locked id)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (map? (:result rslt)))
      (is (= "locked" (get-in rslt [:result :id_status])))
      (is (= id (get-in rslt [:result :user_id])))))
  (testing "Set user status to contact"
    (let [id 3
          rslt (sut/set-user-status-with-id :contact id)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (map? (:result rslt)))
      (is (= "contact" (get-in rslt [:result :id_status])))
      (is (= id (get-in rslt [:result :user_id])))))
  (testing "Setting non-existent user status returns nil"
    (let [id 0
          rslt (sut/set-user-status-with-email :archive id)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :error (:status rslt)))
      (is (nil? (:result rslt))))))

(deftest delete-user-with-email
  (testing "Delete existing user with provided email."
    (let [rslt (sut/delete-user-with-email "fred@example.com")]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (not (nil? (:result rslt))))
      (is (contains? (:result rslt) :email))
      (is (= "fred@example.com" (get-in rslt [:result :email])))))
  (testing "Delete non-existing user given email."
    (let [rslt (sut/delete-user-with-email "fred@example.com")]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (nil? (:result rslt))))))

(deftest delete-user-with-id
  (testing "Delete existing user with provided user id."
    (let [user-id (get-in (sut/add-user "pdutton@voldamort.com"
                                        "Peter" "Dutton"
                                        "I'm a wanker")
                          [:result :user_id])
          rslt (sut/delete-user-with-id user-id)]
      (println (str "testing delete-user-with-id result: " rslt))
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (not (nil? (:result rslt))))
      (is (contains? (:result rslt) :email))
      (is (= "pdutton@voldamort.com" (get-in rslt [:result :email])))))
  (testing "Delete non-existing user given user id."
    (let [rslt (sut/delete-user-with-id 0)]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (nil? (:result rslt))))))

(deftest add-request-record
  (testing "Add confirm request record for user."
    (let [key (str (random-uuid))
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
    (let [key (str (random-uuid))
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
    (let [key (str (random-uuid))
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
          key (str (random-uuid))
          rslt1 (sut/add-request-record user-id key "confirm")]
      (is (= :ok (:status rslt1)))
      (if (= :ok (:status rslt1))
        (let [rslt2 (sut/get-request-record user-id key)]
          (is (map? rslt2))
          (is (contains? rslt2 :status))
          (is (contains? rslt2 :result))
          (is (= :ok (:status rslt2)))
          (is (map? (:result rslt2)))
          (is (= user-id (get-in rslt2 [:result :user_id])))
          (is (= key (get-in rslt2 [:result :req_key])))
          (is (= "confirm" (get-in rslt2 [:result :req_type]))))))))

(defn clear-ids []
  (sut/delete-user-with-email "fred@example.com")
  (sut/truncate-table "auth.roles"))

(defn test-ns-hook []
  (clear-ids)
  (get-user-with-email)
  (get-user-with-id)
  (add-user)
  (set-user-status-with-email)
  (set-user-status-with-id)
  (delete-user-with-email)
  (delete-user-with-id)
  (add-request-record)
  ;; (add-confirm-record)
  ;; (set-confirm-flag)
  )
