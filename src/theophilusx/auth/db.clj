(ns theophilusx.auth.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.connection :as connection]
            [next.jdbc.types :as types]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [theophilusx.auth.db-errors :refer [error-state->code]]
            [theophilusx.auth.log :as log]
            [integrant.core :as ig])
  (:import [java.sql SQLException]
           (com.mchange.v2.c3p0 ComboPooledDataSource)))

(def database (atom nil))

(defmethod ig/init-key :theophilusx.auth.db/data-source [_ config]
  (log/debug (str "data-source: Init connection pool. config = " config))
  (reset! database (connection/->pool ComboPooledDataSource config))
  config)

(defmethod ig/halt-key! :theophilusx.auth.db/data-source [_ _]
  (log/debug "data-source: Closing connection pool")
  (.close @database)
  (reset! database nil))

(defn execute
  "Execute an SQL statement- Return a map with keys `:db-status` and `:result`.
  A `:db-status` of `:ok` indicates SQL executed without error. A value of `:error`
  indicates an SQL error and a key `:error-msg` will include the error.
  The `:result` key is a list of maps representing result rows."
  [sql]
  (log/debug (str "execute: SQL = " sql))
  (try
    (jdbc/execute! @database sql {:return-keys true
                                  :builder-fn  rs/as-unqualified-maps})
    (catch SQLException e
      (log/error (str "execute: SQL: " sql " Ex: " e))
      (throw (ex-info (ex-message e)
                      (assoc (error-state->code e)
                             :sql sql) e)))))

(defn execute-one
  "Execute an SQL statement, expecting a single row result- Returns a map with
  keys `:db-status` and `:result`. A `:db-=status` of `:ok` indicates successful
  execution of SQL statuement- A value of `:error` indicates an SQL error and the key
  `:error-msg` will contain the SQL error message- The `:result` key will contain a map
  representing the row result from the SQL statement."
  [sql]
  (log/debug (str "execute-one: SQL = " sql))
  (try
    (jdbc/execute-one! @database sql {:return-keys true
                                      :builder-fn  rs/as-unqualified-maps})
    (catch SQLException e
      (let [msg (str "execute-one: " (ex-message e))]
        (log/error msg e)
        (throw (ex-info msg
                        (assoc (error-state->code e)
                               :sql sql)))))))

(defn connection-ok?
  "Simple test of database connection."
  []
  (let [rslt (execute-one (sql/format {:select [["Hello" :test]]}))]
    (= (get-in rslt [:result :test]) "Hello")))

(defn truncate-table [table]
  (execute-one (sql/format {:truncate (keyword table)})))

(defn enum [val type]
  [:cast (name val) type])

(defn user-where [user]
  (cond
    (string? user) (h/where [:= :email user])
    (int? user)    (h/where [:= :user_id user])
    :else          (throw (Exception. (str "user-where: Bad argument. "
                                           "argument must be an email address (string) "
                                           "or user ID (integer)")))))

(defn get-user
  "Retrieve a user record.
  Argument is either a string specifying an email address or
  an integer specifying a user ID."
  [user]
  (try
    (let [sql (-> (user-where user)
                  (h/select :*)
                  (h/from :auth.users)
                  (sql/format))]
      (execute-one sql))
    (catch Exception e
      (let [msg (str "get-user: Failed to get " user)]
        (log/error msg e)
        (throw (ex-info msg {:user user} e))))))

(defn add-user
  "Create a new user record."
  [email first-name last-name password & {:keys [modified-by] :or {modified-by "system"}}]
  (try
    (let [sql (-> (h/insert-into :auth.users)
                  (h/columns :email :first_name :last_name :password :created_by :modified_by)
                  (h/values [[email first-name last-name password modified-by modified-by]])
                  (sql/format))]
      (execute-one sql))
    (catch Exception e
      (let [msg (str "add-user: Failed to add user " email)]
        (log/error msg e)
        (throw (ex-info msg
                        {:emagili    email
                         :first-name first-name
                         :last-name  last-name
                         :password   password} e))))))

(defn set-user-status
  "Set the user status for specific user.
  The status argument is one of the keywords
  :confirmed, :locked, :contact or :archived.
  The user argument is either a string specifying email address
  or an integer specifying user ID. The optional :modified-by
  keyword argument specifies who modfied this record and defaults to 'system'."
  [status user & {:keys [modified-by] :or {modified-by "system"}}]
  (try 
    (let [sql (-> (user-where user)
                  (h/update :auth.users)
                  (h/set {:id_status   (enum status :auth.status)
                          :modified_by modified-by
                          :modified_dt :current_timestamp})
                  (sql/format))]
      (execute-one sql))
    (catch Exception e
      (let [msg (str "set-user-status: Failed to set status for " user)]
        (log/error msg e)
        (throw (ex-info msg
                        {:status status
                         :user   user} e))))))

(defn delete-user
  "Delete a user account.
  Argument is either an email address (string) or a
  user ID (integer)."
  [user]
  (try
    (let [sql1 (if (string? user)
                 (-> (h/delete-from :auth.roles)
                     (h/using :auth.users)
                     (h/where [:= :auth.roles.user_id :auth.users.user_id]
                              [:= :auth.users.email user])
                     (sql/format))
                 (-> (h/delete-from :auth.roles)
                     (h/where [:= :user_id user])
                     (sql/format)))
          sql2 (if (string? user)
                 (-> (h/delete-from :auth.requests)
                     (h/using :auth.users)
                     (h/where [:= :auth.requests.user_id :auth.users.user_id]
                              [:= :auth.users.email user])
                     (sql/format))
                 (-> (h/delete-from :auth.requests)
                     (h/where [:= :user_id user])
                     (sql/format)))
          sql3 (-> (user-where user)
                   (h/delete-from :auth.users)
                   (sql/format))]
      (jdbc/with-transaction [tx @database]
        (let [rs1 (jdbc/execute-one! tx sql1)
              rs2 (jdbc/execute-one! tx sql2)
              rs3 (jdbc/execute-one! tx sql3)]
          {:update-count (:next.jdbc/update-count rs3)})))
    (catch Exception e
      (let [msg (str "delete-user: Failed to remove user " user)]
        (log/error msg e)
        (throw (ex-info msg {:user user} e))))))

(defn add-request-record
  "Add a new request record for user account.
  Argument user-id = unique integer ID for specific user,
  req-key = unique string key and req-type is a keyword or string
  specifying the record type. Optional keyword argument :created-by is used
  to specify the created by field for this record."
  [user-id req-key req-type & {:keys [created-by] :or {created-by "system"}}]
  (try
    (let [sql (-> (h/insert-into :auth.requests)
                  (h/columns :user_id :req_key :req_type :created_by)
                  (h/values [[user-id req-key (name req-type) created-by]])
                  (sql/format))]
      (execute-one sql))
    (catch Exception e
      (let [msg (str "add-request-record: Failed to add record for " user-id)]
        (log/error msg e)
        (throw (ex-info msg
                        {:user user-id
                         :key  req-key
                         :type req-type} e))))))

(defn get-request-record
  "Return the request record associated with provided user ID and key."
  [user-id req-key]
  (try
    (let [sql (-> (h/select :*)
                  (h/from :auth.requests)
                  (h/where [:= :user_id user-id]
                           [:= :req_key req-key])
                  (sql/format))]
      (execute-one sql))
    (catch Exception e
      (let [msg (str "get-request-record: Failed to get request record for " user-id)]
        (log/error msg e)
        (throw (ex-info msg
                        {:user user-id
                         :key  req-key} e)))))) 

(defn get-open-user-requests
  "Return list of open requests for provided user ID."
  [user-id]
  (try
    (let [sql (-> (h/select :*)
                  (h/from :auth.requests)
                  (h/where [:= :user_id user-id]
                           [:= :completed false])
                  (sql/format))]
      (execute sql))
    (catch Exception e
      (let [msg (str "get-open-user-requests: Failed to get requests for user " user-id)]
        (log/error msg e)
        (throw (ex-info msg {:user user-id} e))))))

(defn complete-request-record
  "Set the completion status for the request record identified by user id and request key."
  [user-id req-key & {:keys [completed-by remote-addr] :or {completed-by "system"
                                                            remote-addr  "0.0.0.0"}}]
  (try
    (let [sql (-> (h/update :auth.requests)
                  (h/set {:completed    true
                          :completed_dt :current_timestamp
                          :completed_by completed-by
                          :remote_addr  remote-addr})
                  (h/where [:= :user_id user-id]
                           [:= :req_key req-key])
                  (sql/format))]
      (execute-one sql))
    (catch Exception e
      (let [msg (str "complete-request-rec: Failed to complete record for user " user-id)]
        (log/error msg e)
        (throw (ex-info msg {:user user-id :key req-key} e))))))

(defn get-message
  "Retrieve message from message table given message name.
  Argument can be either a keyhword or string specifying the message name."
  [msg-name]
  (try
    (let [sql (-> (h/select :message)
                  (h/from :auth.messages)
                  (h/where [:= :msg_name (name msg-name)])
                  (sql/format))]
      (execute-one sql))
    (catch Exception e
      (let [msg (str "get-message: Failed to get message for " msg-name)]
        (log/error msg e)
        (throw (ex-info msg {:msg-name msg-name} e))))))

(comment
  (-> (h/select :message)
      (h/from :auth.messages)
      (h/where [:= :msg_name (name :confirm)])
      (sql/format)) 
  )




