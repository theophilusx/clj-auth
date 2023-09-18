(ns theophilusx.auth.db-test
  (:require [clojure.test :refer [deftest is testing]]
            [theophilusx.auth.db :as sut]))

(deftest get-id
  (testing "Get known ID"
    (let [rslt (sut/get-id "john@example.com")]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (not (nil? (:result rslt))))
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
    (let [rslt (sut/get-id "unknown@exmaple.com")]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :error (:status rslt)))
      (is (nil? (:result rslt)))
      (is (not (nil? (:error-code rslt)))))))

(deftest add-id
  (testing "Add new ID"
    (let [rslt (sut/add-id "fred@example.com" "Fred" "Flintstone" "Fred's secret")]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (not (nil? (:result rslt))))
      (is (contains? (:result rslt) :email))
      (is (= "fred@example.com" (get-in rslt [:result :email])))
      (is (contains? (:result rslt) :first_name))
      (is (= "Fred" (get-in rslt [:result :first_name])))
      (is (contains? (:result rslt) :last_name))
      (is (= "Flintstone" (get-in rslt [:result :last_name])))
      (is (contains? (:result rslt) :password))
      (is (= "Fred's secret" (get-in rslt [:result :password])))))
  (testing "Add conflicting ID"
    (let [rslt (sut/add-id "fred@example.com" "Fred2" "Flintstone" "Fred2's secret")]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :error-code))
      (is (contains? rslt :result))
      (is (= :error (:status rslt)))
      (is (nil? (:result rslt)))
      (is (not (nil? (:error-code rslt))))
      (is (= :error (:status rslt)))
      (is (= "23505" (:error-code rslt))))))

(deftest delete-id
  (testing "Delete existing ID"
    (let [rslt (sut/delete-id "fred@example.com")]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (not (nil? (:result rslt))))
      (is (contains? (:result rslt) :email))
      (is (= "fred@example.com" (get-in rslt [:result :email])))))
  (testing "Delete non-existing ID"
    (let [rslt (sut/delete-id "fred@example.com")]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (nil? (:result rslt))))))
      
(deftest add-confirm-record
  (let [id (random-uuid)
        email "fred@example.com"]
    (testing "Add new confirmation record"
      (let [rslt (sut/add-confirm-record email id)]
        (is (map? rslt))
        (is (contains? rslt :status))
        (is (contains? rslt :result))
        (is (= :ok (:status rslt)))
        (is (not (nil? (:result rslt))))
        (is (= email (get-in rslt [:result :email])))
        (is (= (.toString id) (get-in rslt [:result :confirm_id])))))
    (testing "Add 2nd confirmation record for same email"
      (let [id2 (random-uuid)
            rslt (sut/add-confirm-record email id2)]
        (is (map? rslt))
        (is (contains? rslt :status))
        (is (contains? rslt :result))
        (is (= :ok (:status rslt)))
        (is (not (nil? (:result rslt))))
        (is (= email (get-in rslt [:result :email])))
        (is (= (.toString id2) (get-in rslt [:result :confirm_id])))))
    (testing "Fail to add duplicate ID 1"
      (let [rslt (sut/add-confirm-record email id)]
        (is (map? rslt))
        (is (contains? rslt :status))
        (is (contains? rslt :result))
        (is (contains? rslt :error-name))
        (is (contains? rslt :error-code))
        (is (= :error (:status rslt)))
        (is (nil? (:result rslt)))
        (is (= "23505" (:error-code rslt)))
        (is (= :db-unique-violation (:error-name rslt)))))))

(deftest set-confirm-flag
  (let [id1 (random-uuid)
        email "fred@example.com"
        ip "192.168.1.1"]
    (testing "Set confirmation flag"
      (let [r1 (sut/add-confirm-record email id1)]
        (is (= :ok (:status r1)))
        (let [r2 (sut/set-confirm-flag (.toString id1) email ip)]
          (is (map? r2))
          (is (contains? r2 :status))
          (is (contains? r2 :result))
          (is (= :ok (:status r2)))
          (is (not (nil? (:result r2))))
          (is (contains? (:result r2) :confirm_id))
          (is (contains? (:result r2) :email))
          (is (contains? (:result r2) :is_confirmed))
          (is (= (.toString id1) (get-in r2 [:result :confirm_id])))
          (is (= email (get-in r2 [:result :email])))
          (is (= true (get-in r2 [:result :is_confirmed]))))))
    (testing "Fail with different email"
      (let [email "joe@example.com"
            rslt (sut/set-confirm-flag (.toString id1) email ip)]
        (is (map? rslt))
        (is (contains? rslt :status))
        (is (contains? rslt :result))
        (is (contains? rslt :error-msg))
        (is (contains? rslt :error-name))
        (is (contains? rslt :error-code))
        (is (= :error (:status rslt)))
        (is (= "No matching confirmation ID" (:error-msg rslt)))
        (is (= :not-found (:error-name rslt)))
        (is (= "-1" (:error-code rslt)))
        (is (nil? (:result rslt)))))))

(defn clear-ids []
  (sut/delete-id "fred@example.com")
  (sut/truncate-table "auth.confirm"))

(defn test-ns-hook []
  (get-id)
  (clear-ids)
  (add-id)
  (delete-id)
  (add-confirm-record)
  (set-confirm-flag))
