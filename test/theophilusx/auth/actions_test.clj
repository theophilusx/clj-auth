(ns theophilusx.auth.actions-test
  (:require [clojure.test :refer [deftest is testing]]
            [theophilusx.auth.actions :as sut]
            [theophilusx.auth.db :as db]
            [clojure.string :refer [starts-with?]]))

(deftest create-id
  (let [email "barney@example.com"
        fname "Barney"
        lname "Rubble"
        pwd "Barney's secret"]
    (testing "Create new ID"
      (let [rslt (sut/create-id email fname lname pwd)]
        (is (map? rslt))
        (is (contains? rslt :status))
        (is (contains? rslt :result))
        (is (= :ok (:status rslt)))
        (is (not (nil? (:result rslt))))
        (is (contains? (:result rslt) :email))
        (is (contains? (:result rslt) :state))
        (is (= email (get-in rslt [:result :email])))
        (is (= :verify (get-in rslt [:result :state])))))
    (testing "Attempt create existing ID"
      (let [rslt (sut/create-id email fname lname pwd)]
        (is (map? rslt))
        (is (contains? rslt :status))
        (is (contains? rslt :result))
        (is (contains? rslt :error-code))
        (is (contains? rslt :error-name))
        (is (contains? rslt :error-msg))
        (is (= :error (:status rslt)))
        (is (= :db-unique-violation (:error-name rslt)))
        (is (= "23505" (:error-code rslt)))
        (is (starts-with? (:error-msg rslt)
                          "ERROR: duplicate key value violates unique constraint"))))))

(deftest create-verify-record
  (let [email "barney@example.com"]
    (testing "Creation of verify record"
      (let [rslt (sut/create-verify-record email)]
        (is (map? rslt))
        (is (contains? rslt :status))
        (is (contains? rslt :email))
        (is (contains? rslt :vid))
        (is (= :ok (:status rslt)))
        (is (= email (:email rslt)))))))

(defn clear-ids []
  (println "Clear test IDs")
  (db/delete-id "barney@example.com"))

(defn test-ns-hook []
  (println "Running test-ns-hook")
  (clear-ids)
  (create-id)
  (create-verify-record))


