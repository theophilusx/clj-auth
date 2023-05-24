(ns theophilusx.auth.db-test
  (:require [clojure.test :refer [deftest is testing]]
            [theophilusx.auth.db :as sut]
            [theophilusx.auth.db :as db]))

(deftest get-id
  (testing "Get known ID"
    (let [rslt (db/get-id "john@example.com")]
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
    (let [rslt (db/get-id "unknown@exmaple.com")]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :error (:status rslt)))
      (is (nil? (:result rslt)))
      (is (not (nil? (:error-code rslt)))))))

(deftest add-id
  (testing "Add new ID"
    (let [rslt (db/add-id "fred@example.com" "Fred" "Flintstone" "Fred's secret")]
      (is (and (map? rslt)
               (contains? rslt :status)
               (contains? rslt :result)))
      (is (and (= :ok (:status rslt))
               (not (nil? (:result rslt)))))
      (is (and (contains? (:result rslt) :email)
               (= "fred@example.com" (get-in rslt [:result :email]))))
      (is (and (contains? (:result rslt) :first_name)
               (= "Fred" (get-in rslt [:result :first_name]))))
      (is (and (contains? (:result rslt) :last_name)
               (= "Flintstone" (get-in rslt [:result :last_name]))))
      (is (and (contains? (:result rslt) :password)
               (= "Fred's secret" (get-in rslt [:result :password]))))))
  (testing "Add conflicting ID"
    (let [rslt (db/add-id "fred@example.com" "Fred2" "Flintstone" "Fred2's secret")]
      (is (and (map? rslt)
               (contains? rslt :status)
               (contains? rslt :error-code)
               (contains? rslt :result)))
      (is (and (= :error (:status rslt))
               (nil? (:result rslt))
               (not (nil? (:error-code rslt)))))
      (is (= :error (:status rslt)))
      (is (= "23505" (:error-code rslt))))))

(deftest delete-id
  (testing "Delete existing ID"
    (let [rslt (db/delete-id "fred@example.com")]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (not (nil? (:result rslt))))
      (is (contains? (:result rslt) :email))
      (is (= "fred@example.com" (get-in rslt [:result :email])))))
  (testing "Delete non-existing ID"
    (let [rslt (db/delete-id "fred@example.com")]
      (is (map? rslt))
      (is (contains? rslt :status))
      (is (contains? rslt :result))
      (is (= :ok (:status rslt)))
      (is (nil? (:result rslt))))))
      
(defn clear-ids []
  (db/delete-id "fred@example.com"))

(defn test-ns-hook []
  (get-id)
  (clear-ids)
  (add-id)
  (delete-id))
