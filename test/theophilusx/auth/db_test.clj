(ns theophilusx.auth.db-test
  (:require [clojure.test :refer [deftest is testing]]
            [theophilusx.auth.db :as sut]
            [theophilusx.auth.db :as db]))

(deftest get-id
  (testing "Get known ID"
    (let [rslt (db/get-id "john@example.com")]
      (is (= :ok (:db-status rslt)))
      (is (= "john@example.com" (get-in rslt [:result :email])))
      (is (= "John" (get-in rslt [:result :first_name])))
      (is (= "Smith" (get-in rslt [:result :last_name])))))
  (testing "Get unknown ID"
    (let [rslt (db/get-id "unknown@exmaple.com")]
      (is (= :ok (:db-status rslt)))
      (is (nil? (:result rslt))))))

(deftest add-id
  (testing "Add new ID"
    (let [rslt (db/add-id "fred@example.com" "Fred" "Flintstone" "Fred's secret")]
      (is (= :ok (:db-status rslt)))
      (is (= "fred@example.com" (get-in rslt [:result :email])))
      (is (= "Fred" (get-in rslt [:result :first_name])))
      (is (= "Flintstone" (get-in rslt [:result :last_name])))
      (is (= "Fred's secret" (get-in rslt [:result :password])))))
  (testing "Add conflicting ID"
    (let [rslt (db/add-id "fred@example.com" "Fred2" "Flintstone" "Fred2's secret")]
      (is (= :error (:db-status rslt)))
      (is (= "23505" (:error-state rslt))))))

(deftest delete-id
  (testing "Delete existing ID"
    (let [rslt (db/delete-id "fred@example.com")]
      (is (= :ok (:db-status rslt)))
      (is (= "fred@example.com" (get-in rslt [:result :email])))))
  (testing "Delete non-existing ID"
    (let [rslt (db/delete-id "fred@example.com")]
      (is (= :ok (:db-status rslt)))
      (is (nil? (:result rslt))))))
      

(defn test-ns-hook []
  (get-id)
  (add-id)
  (delete-id))
