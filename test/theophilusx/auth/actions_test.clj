(ns theophilusx.auth.actions-test
  (:require [clojure.test :refer [deftest is testing]]
            [theophilusx.auth.actions :as sut]
            [theophilusx.auth.db :as db]
            [clojure.string :refer [starts-with?]]))

(deftest create-request-record
  (testing "Successfully generate request record"
    (let [{{:keys [user_id]} :result} (db/get-user-with-email "john@example.com")
          rslt (sut/create-request-record user_id "confirm")]
      (is (map? rslt))
      (is (= :ok (:status rslt)))
      (is (= (set (keys rslt)) #{:status :user-id :vid :type}))))
  (testing "Fail generation of request record"
    (let [rslt (sut/create-request-record -1 "verify")]
      (is (map? rslt))
      (is (= :error (:status rslt)))
      (is (nil? (:result rslt))))))

(deftest request-confirm-id
  (let [email "john@example.com"
        {{:keys [user_id]} :result} (db/get-user-with-email email)]
    (testing "SUccessfully send ID verification message"
      (let [rslt (sut/request-confirm-id email user_id)]
        (is (map? rslt))
        (is (contains? rslt :status))
        (is (= :ok (:status rslt)))))
    (testing "Fail to send ID verification to unknown id"
      (let [rslt (sut/request-confirm-id "unknown@someplace.com" -1)]
        (is (map? rslt))
        (is (contains? rslt :status))
        (is (= :error (:status rslt)))))))

(deftest create-id
  (let [email "barney@example.com"
        fname "Barney"
        lname "Rubble"
        pwd   "Barney's secret"]
    (testing "Create new ID"
      (let [rslt (sut/create-id email fname lname pwd)]
        (is (map? rslt))
        (is (contains? rslt :status))
        (is (contains? rslt :result))
        (is (= :ok (:status rslt)))
        (is (some? (:result rslt)))
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

;; (deftest verify-id 
;;   (testing "Successfully verify ID"
;;     (let [])
;;     (is (= assertion-values)))) 

(defn clear-ids []
  (println "Clear test IDs")
  (db/delete-user-with-email "barney@example.com"))

(defn test-ns-hook []
  (println "Running test-ns-hook")
  (create-request-record)
  (request-confirm-id)
  (clear-ids)
  (create-id)
  ;; (create-verify-record)
  ;; (request-verify-id)
  )


