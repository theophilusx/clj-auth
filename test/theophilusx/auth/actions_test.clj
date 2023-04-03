(ns theophilusx.auth.actions-test
  (:require [clojure.test :refer [deftest is testing]]
            [theophilusx.auth.actions :as sut]
            [theophilusx.auth.mocks :as mock]
            [theophilusx.auth.db :as db]))

(deftest user-registration
  (with-redefs [db/get-id mock/get-id]
    (testing "create user - ok"
      (let [rslt (sut/create-id "chris@example.com" "Chris" "Chalk" "my secret")]
        (println "Result: " rslt)
        (is (= :ok (:db-status rslt)))))))

(comment
  (run-tests)
  )
