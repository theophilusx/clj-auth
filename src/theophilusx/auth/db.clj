(ns theophilusx.auth.db
  (:require [next.jdbc :as jdbc]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [theophilusx.auth.system :refer [system]])
  (:import [java.sql Connection SQLException]))

(defn data-source []
  (:theophilusx.auth.system/database @system))

(defn test-connection [ds]
  (let [rslt (jdbc/execute! (data-source0gz) ["SELECT 'Hello' test"])]
    (if (= (:test (first rslt)) "Hello")
      true
      false)))

(comment
  (test-connection (data-source))
  (sql/format {:select [['Hello' :test]]})
  (jdbc/execute! (data-source) (sql/format {:select [["Hello" :test]]}))
  )
