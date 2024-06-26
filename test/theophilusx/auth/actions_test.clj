(ns theophilusx.auth.actions-test
  (:require [clojure.test :refer [deftest is testing]]
            [theophilusx.auth.actions :as sut]
            [theophilusx.auth.db :as db]
            [clojure.string :refer [starts-with?]]))

(deftest create-request-record
  (testing "Successfully generate request record"
    (let [{:keys [user_id]} (db/get-user "john@example.com")]
      (is (pos? user_id))
      (let [rslt (sut/create-request-record user_id :confirmed)]
        (is (map? rslt))
        (is (= (set (keys rslt))
               #{:completed_by :req_type :completed :created_dt
                 :req_key :remote_addr :user_id :created_by
                 :completed_dt :req_id})))))
  (testing "Fail generation of request record"
    (is (thrown-with-msg? Exception #"create-request-record; Failed to add :verify for user"
                          (sut/create-request-record -1 :verify)))))

(deftest request-confirm-id
  (testing "Successfully send ID verification msg using email"
    (let [rslt (sut/request-confirm-id "john@example.com")]
      (is (= :success (:mail-status rslt)))))
  (testing "Fail to send ID verification to unknown id"
    (is (thrown? Exception (sut/request-confirm-id "unknown@someplace.com"))))
  (testing "Successfully send ID verifications msg using user ID"
    (let [rslt (sut/request-confirm-id 2)]
      (is (= :success (:mail-status rslt)))))
  (testing "Fail to send ID verificaiton to unknown user ID"
    (is (thrown? Exception (sut/request-confirm-id -1)))))

(deftest create-id
  (let [email "barney@example.com"
        fname "Barney"
        lname "Rubble"
        pwd   "Barney's secret"]
    (testing "Create new ID"
      (let [rslt (sut/create-id email fname lname pwd)]
        (is (= :success (:mail-status rslt)))))
    (testing "Attempt create existing ID"
      (is (thrown? Exception (sut/create-id email fname lname pwd))))))

(deftest verify-id
  (testing "Successfully verify ID with good key"
    (let [email                     "john@example.com"
          {:keys [user_id]}         (db/get-user email)
          {:keys [user_id req_key]} (sut/create-request-record user_id :confirmed)
          rs                        (sut/verify-id user_id req_key "127.0.0.1")]
      (is (map? rs))
      (is (= email (:email rs)))
      (is (= "confirmed" (:id_status rs))))))

(defn clear-ids []
  (try
    (println (str "clear-ids: " (db/delete-user "barney@example.com")))
    (catch Exception e
      (println (str "clear-ids: " (ex-message e)))
      nil)))

(defn test-ns-hook []
  (create-request-record)
  (request-confirm-id)
  (clear-ids)
  (create-id)
  (verify-id))


