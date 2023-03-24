(ns theophilusx.auth.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.connection :as connection]
            [honey.sql :as sql]
            ;;[honey.sql.helpers :as h]
            [taoensso.timbre :as log]
            [theophilusx.auth.utils :refer [read-env]]
            [integrant.core :as ig])
  (:import [java.sql SQLException]
           (com.mchange.v2.c3p0 ComboPooledDataSource)))

(def database (atom nil))

(defmethod ig/prep-key :theophilusx.auth.db/data-source [_ config]
  (log/debug (str "data-source: Prepare key config = " config))
  (let [e (read-env "resources/.env.edn")]
    (merge {:user (:db-user e) :password (:db-password e)} config)))

(defmethod ig/init-key :theophilusx.auth.db/data-source [_ config]
  (let [e (read-env "resources/.env.edn")
        c (merge {:user (:db-user e) :password (:db-password e)} config)]
    (log/debug (str "data-source: Init connection pool. config = " config))
    (reset! database (connection/->pool ComboPooledDataSource config))
    c))
  
(defmethod ig/halt-key! :theophilusx.auth.db/data-source [_ _]
  (log/debug "data-source: Closing connection pool")
  (.close @database)
  (reset! database nil))

(defn execute
  "Execute an SQL statement. Return a map with keys `:db-status` and `:result`.
  A `:db-status` of `:ok` indicates SQL executed without error. A value of `:error`
  indicates an SQL error and a key `:error-msg` will include the error.
  The `:result` key is a list of maps representing result rows."
  [sql]
  (log/debug (str "execute: SQL = " sql))
  (try
    (let [rslt (jdbc/execute! @database sql {:return-keys true
                                             :builder-fn rs/as-unqualified-maps})]
      {:db-status :ok
       :result rslt})
    (catch SQLException e
      (log/debug (str "execute: Error = " (.getMessage e)))
      {:db-status :error
       :error-msg (.getMessage e)})))

(defn execute-one
  "Execute an SQL statement, expecting a single row result. Returns a map with
  keys `:db-status` and `:result`. A `:db-=status` of `:ok` indicates successful
  execution of SQL statuement. A value of `:error` indicates an SQL error and the key
  `:error-msg` will contain the SQL error message. The `:result` key will contain a map
  representing the row result from the SQL statement."
  [sql]
  (log/debug (str "execute-one: SQL = " sql))
  (try
    (let [rslt (jdbc/execute-one! @database sql {:return-keys true
                                                 :builder-fn rs/as-unqualified-maps})]
      {:db-status :ok
       :result rslt})
    (catch SQLException e
      (log/debug (str "execute-one: Error = " (.getMessage e)))
      {:db-status :error
       :error-msg (.getMessage e)})))

(defn connection-ok?
  "Simple test of database connection."
  []
  (let [rslt (execute-one (sql/format {:select [["Hello" :test]]}))]
    (= (get-in rslt [:result :test]) "Hello")))

(defn get-user
  "Retrieve a user record based on email primary key."
  [email]
  (let [sql (sql/format {:select [:*]
                         :from :auth.users
                         :where [:= :email email]})]
    (execute-one sql)))

(defn add-user
  "Create a new user record."
  [email first-name last-name password & {:keys [modified-by] :or {modified-by "system"}}]
  (let [sql (sql/format {:insert-into [:auth.users]
                         :columns [:email :first_name :last_name :password :created_by :modified_by]
                         :values [[email first-name last-name password modified-by modified-by]]})
        rslt (execute-one sql)]
    (if (and (= :ok (:db-status rslt))
             (= (get-in rslt [:result :update-count]) 1))
      (get-user email)
      rslt)))

(comment
  )
