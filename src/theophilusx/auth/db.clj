(ns theophilusx.auth.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [next.jdbc.connection :as connection]
            [honey.sql :as sql]
            [honey.sql.helpers :as h]
            [taoensso.timbre :as log]
            [theophilusx.auth.utils :refer [read-env]]
            [integrant.core :as ig])
  (:import [java.sql SQLException]
           (com.mchange.v2.c3p0 ComboPooledDataSource)))

(def database (atom nil))

(defmethod ig/init-key :theophilusx.auth.db/data-source [_ config]
  (let [e (read-env "resources/.env.edn")
        c (merge {:user (:db-user e) :password (:db-password e) :dbname (:db-name e)} config)]
    (log/debug (str "data-source: Init connection pool. config = " config))
    (reset! database (connection/->pool ComboPooledDataSource c))
    c))

(defmethod ig/halt-key! :theophilusx.auth.db/data-source [_ _]
  (log/debug "data-source: Closing connection pool")
  (.close @database)
  (reset! database nil))

(defn error-state->code [state]
  (condp re-matches state
    #"^0100C$" {:error :db-dynamic-result-sets-returned :value state}
    #"^01008$" {:error :db-implicit-zero-bit-padding :value state}
    #"^01003$" {:error :db-null-value-eliminated-in-set-function :value state}
    #"^01007$" {:error :db-privilege-not-granted :value state}
    #"^01007$" {:error :db-privilege-not-revoked :value state}
    #"^01004$" {:error :db-string-data-right-truncation :value state}
    #"^01P01$" {:error :db-deprecated-feature :value state}
    #"^02-*$" {:error :db-no-data :value state}
    #"^03000$" {:error :db-sql-statement-not-yet-complete :value state}
    #"^08003$" {:error :db-connection-does-not-exist :value state}
    #"^08006$" {:error :db-connection-failure :value state}
    #"^08001$" {:error :db-sqlclient-unable-to-establish-sqlconnection :value state}
    #"^08004$" {:error :db-sqlserver-rejected-establishment-of-sqlconnection :value state}
    #"^08007$" {:error :db-transaction-resolution-unknown :value state}
    #"^08P01$" {:error :db-protocol-violation :value state}
    #"^09000$" {:error :db-triggered-action-exception :value state}
    #"^0A000$" {:error :db-feature-not-supported :value state}
    #"^0B000$" {:error :db-invalid-transaction-initiation :value state}
    #"^0F-*$" {:error :db-locator-exception :value state}
    #"^0L-*$" {:error :db-invalid-grantor :value state}
    #"^0P000$" {:error :db-invlaid-role-specification :value state}
    #"^0Z-*$" {:error :db-diagnostics-exception :value state}
    #"^20-*$" {:error :db-case-not-found :value state}
    #"^21-*$" {:error :db-cardinality-violation :value state}
    #"^22000$" {:error :db-data-exception :value state}
    #"^2202E$" {:error :db-array-subscript-error :value state}
    #"^22021$" {:error :db-character-not-in-repertoire :value state}
    #"^22008$" {:error :db-datetime-field-overflow :value state}
    #"^22012$" {:error :db-division-by-zero :value state}
    #"^22005$" {:error :db-error-in-assignment :value state}
    #"^2200B$" {:error :db-escape-character-conflict :value state}
    #"^22022$" {:error :db-indicator-overflow :value state}
    #"^22015$" {:error :db-interval-field-overflow :value state}
    #"^2201E$" {:error :db-invalid-argument-for-logarithm :value state}
    #"^22014$" {:error :db-invalid-argument-for-ntile-function :value state}
    #"^22016$" {:error :db-invalid-argument-for-nth-value-function :value state}
    #"^2201F$" {:error :db-invalid-argument-for-power-function :value state}
    #"^2201G$" {:error :db-invalid-argument-for-width-bucket-function :value state}
    #"^22018$" {:error :db-invalid-character-value-for-cast :value state}
    #"^22007$" {:error :db-invalid-datetime-format :value state}
    #"^22019$" {:error :db-invalid-escape-character :value state}
    #"^2200D$" {:error :db-invalid-escape-octet :value state}
    #"^22025$" {:error :db-invalid-escape-sequence :value state}
    #"^22P06$" {:error :db-nonstandard-use-of-escape-character :value state}
    #"^22010$" {:error :db-invalid-indicator-parameter-value :value state}
    #"^22023$" {:error :db-invalid-parameter-value :value state}
    #"^2201B$" {:error :db-invalid-regular-expression :value state}
    #"^2201W$" {:error :db-invalid-row-count-in-limit-clause :value state}
    #"^2201X$" {:error :db-invalid-row-count-in-result-offset-clause :value state}
    #"^2202H$" {:error :db-invalid-tablesample-argument :value state}
    #"^2202G$" {:error :db-invalid-tablesample-repeat :value state}
    #"^22009$" {:error :db-invalid-time-zone-displacement-value :value state}
    #"^2200C$" {:error :db-invalid-use-of-escape-character :value state}
    #"^2200G$" {:error :db-most-specific-type-mismatch :value state}
    #"^22004$" {:error :db-null-value-not-allowed :value state}
    #"^22002$" {:error :db-null-value-no-indicator-parameter :value state}
    #"^22003$" {:error :db-numeric_value_out_of_range :value state}
    #"^22026$" {:error :db-string-data-length-mismatch :value state}
    #"^22001$" {:error :db-string_data_right_truncation :value state}
    #"^22011$" {:error :db-substring_error :value state}
    #"^22027$" {:error :db-trim-error :value state}
    #"^22024$" {:error :db-unterminated-c-string :value state}
    #"^2200F$" {:error :db-zero-length-character-string :value state}
    #"^22P01$" {:error :db-floating-point-exception :value state}
    #"^22P02$" {:error :db-invalid-text-representation :value state}
    #"^22P03$" {:error :db-invalid-binary-representation :value state}
    #"^22P04$" {:error :db-bad-copy-file-format :value state}
    #"^22P05$" {:error :db-untranslatable-character :value state}
    #"^2200L$" {:error :db-not-an-xml-document :value state}
    #"^2200M$" {:error :db-invalid-xml-document :value state}
    #"^2200N$" {:error :db-invalid-xml-content :value state}
    #"^2200S$" {:error :db-invalid-xml-comment :value state}
    #"^2200T$" {:error :db-invalid-xml-processing-instruction :value state}
    #"^23000$" {:error :db-integrity-constraint-violation :value state}
    #"^23001$" {:error :db-restrict-violation :value state}
    #"^23502$" {:error :db-not-null-violation :value state}
    #"^23503$" {:error :db-foreign-key-violation :value state}
    #"^23505$" {:error :db-unique-violation :value state}
    #"^23514$" {:error :db-check-violation :value state}
    #"^23P01$" {:error :db-exclusion-violation :value state}
    #"^24000$" {:error :db-invalid-cursor-state :value state}
    #"^25-*$" {:error :db-invalid-transaction-state :value state}
    #"^26-*$" {:error :db-invalid-sql-statement-name :value state}
    #"^27-*$" {:error :db-triggered-data-change-violation :value state}
    #"^28000$" {:error :db-invalid-authorization-specification :value state}
    #"^28P01$" {:error :db-invalid-password :value state}
    #"^2B-*$" {:error :db-dependent-privilege-descriptions-still-exist :value state}
    #"^2D000$" {:error :db-invalid-transaction-termination :value state}
    #"^2F-*$" {:error :db-sql-routine-exception :value state}
    #"^34000$" {:error :db-invalid-cursor-name :value state}
    #"^38-*$" {:error :db-external-routine-exception :value state}
    #"^39-*$" {:error :db-external-routine-invocation-exception :value state}
    #"^3B-*$" {:error :db-savepoint-exception :value state}
    #"^3D000$" {:error :db-invalid-catalogue-name :value state}
    #"^3F000$" {:error :db-invalid-schema-name :value state}
    #"^40-*$" {:error :db-transaction-rollback :value state}
    #"^42000$" {:error :db-syntax-error-or-access-rule-violation :value state}
    #"^42601$" {:error :db-syntax-error :value state}
    #"^42501$" {:error :db-insufficient-privilege :value state}
    #"^42846$" {:error :db-cannot-coerce :value state}
    #"^42803$" {:error :db-grouping-error :value state}
    #"^42P20$" {:error :db-windowing-error :value state}
    #"^42P19$" {:error :db-invalid-recursion :value state}
    #"^42830$" {:error :db-invalid-foreign-key :value state}
    #"^42602$" {:error :db-invalid-name :value state}
    #"^42622$" {:error :db-name-too-long :value state}
    #"^42939$" {:error :db-reserved-name :value state}
    #"^42804$" {:error :db-datatype-mismatch :value state}
    #"^42P18$" {:error :db-indeterminate-datatype :value state}
    #"^42P21$" {:error :db-collation-mismatch :value state}
    #"^42P22$" {:error :db-indeterminate-collation :value state}
    #"^42809$" {:error :db-wrong-object-type :value state}
    #"^42703$" {:error :db-undefined-column :value state}
    #"^42883$" {:error :db-undefined-function :value state}
    #"^42P01$" {:error :db-undefined-table :value state}
    #"^42P02$" {:error :db-undefined-parameter :value state}
    #"^42704$" {:error :db-undefined-object :value state}
    #"^42701$" {:error :db-duplicate-column :value state}
    #"^42P03$"  {:error :db-uplicate-cursor :value state}
    #"^42P04$" {:error :db-duplicate-database :value state}
    #"^42723$" {:error :db-duplicate-function :value state}
    #"^42P05$" {:error :db-duplicate-prepared-statement :value state}
    #"^42P06$" {:error :db-duplicate-schema :value state}
    #"^42P07$" {:error :db-duplicate-table :value state}
    #"^42712$" {:error :db-duplicate-alias :value state}
    #"^42710$" {:error :db-duplicate-object :value state}
    #"^42702$" {:error :db-ambiguous-column :value state}
    #"^42725$" {:error :db-ambiguous-function :value state}
    #"^42P08$" {:error :db-ambiguous-parameter :value state}
    #"^42P09$" {:error :db-ambiguous-alias :value state}
    #"^42P10$" {:error :db-invalid-column-reference :value state}
    #"^42611$" {:error :db-invalid-column-definition :value state}
    #"^42P11$" {:error :db-invalid-cursor-definition :value state}
    #"^42P12$" {:error :db-invalid-database-definition :value state}
    #"^42P13$"  {:error :db-invalid-function-definition :value state}
    #"^42P14$" {:error :db-invalid-prepared-statement-definition :value state}
    #"^42P15$" {:error :db-invalid-schema-definition :value state}
    #"^42P16$" {:error :db-invalid-table-definition :value state}
    #"^42P17$" {:error :db-invalid-object-definition :value state}
    #"^44000$" {:error :db-with-check-option-violation :value state}
    #"^53000$" {:error :db-insufficient-resources :value state}
    #"^53100$" {:error :db-disk-full :value state}
    #"^53200$" {:error :db-out-of-memory :value state}
    #"^53300$" {:error :db-too-many-connections :value state}
    #"^53400$" {:error :db-configuration-limit-exceeded :value state}
    #"^54000$" {:error :db-program-limit-exceeded :value state}
    #"^54001$" {:error :db-statement-too-complex :value state}
    #"^54011$" {:error :db-too-many-columns :value state}
    #"^54023$" {:error :db-too-many-arguments :value state}
    #"^55000$" {:error :db-object-not-in-prerequisite-state :value state}
    #"^55006$" {:error :db-object-in-use :value state}
    #"^55P02$" {:error :db-cant-change-runtime-param :value state}
    #"^55P03$" {:error :db-lock-not-available :value state}
    #"^55P04$" {:error :db-unsafe-new-enum-value-usage :value state}
    #"^57000$" {:error :db-operator-intervention :value state}
    #"^57014$" {:error :db-query-canceled :value state}
    #"^57P01$" {:error :db-admin-shutdown :value state}
    #"^57P02$" {:error :db-crash-shutdown :value state}
    #"^57P03$" {:error :db-cannot-connect-now :value state}
    #"^57P04$" {:error :db-database-dropped :value state}
    #"^57P05$" {:error :db-idle-session-timeout :value state}
    #"^58000$" {:error :db-system_error :value state}
    #"^58030$" {:error :db-io-error :value state}
    #"^58P01$" {:error :db-undefined-file :value state}
    #"^58P02$" {:error :db-duplicate-file :value state}
    #"^72000$" {:error :db-snapshot-too-old :value state}
    #"^F0000$" {:error :db-config-file-error :value state}
    #"^F0001$" {:error :db-lock-file-exists :value state}
    #"^HV000$" {:error :db-fdw-error :value state}
    #"^HV005$" {:error :db-fdw-column-name-not-found :value state}
    #"^HV002$" {:error :db-fdw-dynamic-parameter-value-needed :value state}
    #"^HV010$" {:error :db-fdw-function-sequence-error :value state}
    #"^HV021$" {:error :db-fdw-inconsistent-descriptor-information :value state}
    #"^HV024$" {:error :db-fdw-invalid-attribute-value :value state}
    #"^HV007$" {:error :db-fdw-invalid-column-name :value state}
    #"^HV008$" {:error :db-fdw-invalid-column-number :value state}
    #"^HV004$" {:error :db-fdw-invalid-data-type :value state}
    #"^HV006$" {:error :db-fdw-invalid-data-type-descriptors :value state}
    #"^HV091$" {:error :db-fdw-invalid-descriptor-field-identifier :value state}
    #"^HV00B$" {:error :db-fdw-invalid-handle :value state}
    #"^HV00C$" {:error :db-fdw-invalid-option-index :value state}
    #"^HV00D$" {:error :db-fdw-invalid-option-name :value state}
    #"^HV090$" {:error :db-fdw-invalid-string-length-or-buffer-length :value state}
    #"^HV00A$" {:error :db-fdw-invalid-string-format :value state}
    #"^HV009$" {:error :db-fdw-invalid-use-of-null-pointer :value state}
    #"^HV014$" {:error :db-fdw_too-many-handles :value state}
    #"^HV001$" {:error :db-fdw-out-of-memory :value state}
    #"^HV00P$" {:error :db-fdw-no-schemas :value state}
    #"^HV00J$" {:error :db-fdw-option-name-not-found :value state}
    #"^HV00K$" {:error :db-fdw-reply-handle :value state}
    #"^HV00Q$" {:error :db-fdw-schema-not-found :value state}
    #"^HV00R$" {:error :db-fdw-table-not-found :value state}
    #"^HV00L$" {:error :db-fdw-unable-to-create-execution :value state}
    #"^HV00M$" {:error :db-fdw-unable-to-create-reply :value state}
    #"^HV00N$" {:error :db-fdw-unable-to-establish-connection :value state}
    #"^P0000$" {:error :db-plpgsql-error :value state}
    #"^P0001$" {:error :db-raise-exception :value state}
    #"^P0002$" {:error :db-no-data-found :value state}
    #"^P0003$" {:error :db-too-many-rows :value state}
    #"^P0004$" {:error :db-assert-failure :value state}
    #"^XX000$" {:error :db-internal-error :value state}
    #"^XX001$" {:error :db-data-corrupted :value state}
    #"^XX002$" {:error :db-index-corrupted :value state}
    {:error (keyword (str "db-error-" state)) :value state}))

(defn execute
  "Execute an SQL statement- Return a map with keys `:db-status` and `:result`.
  A `:db-status` of `:ok` indicates SQL executed without error. A value of `:error`
  indicates an SQL error and a key `:error-msg` will include the error.
  The `:result` key is a list of maps representing result rows."
  [sql]
  (log/debug (str "execute: SQL = " sql))
  (try
    (let [rslt (jdbc/execute! @database sql {:return-keys true
                                             :builder-fn rs/as-unqualified-maps})]
      {:status :ok
       :result rslt})
    (catch SQLException e
      (log/error (str "execute: Error = " (.getMessage e)))
      (let [error-data (error-state->code (.getSQLState e))]
        {:status :error
         :error-msg (.getMessage e)
         :error-name (:error error-data)
         :error-code (:value error-data)
         :sql sql
         :result nil}))))

(defn execute-one
  "Execute an SQL statement, expecting a single row result- Returns a map with
  keys `:db-status` and `:result`. A `:db-=status` of `:ok` indicates successful
  execution of SQL statuement- A value of `:error` indicates an SQL error and the key
  `:error-msg` will contain the SQL error message- The `:result` key will contain a map
  representing the row result from the SQL statement."
  [sql]
  (log/debug (str "execute-one: SQL = " sql))
  (try
    (let [rslt (jdbc/execute-one! @database sql {:return-keys true
                                                 :builder-fn rs/as-unqualified-maps})]
      {:status :ok
       :result rslt})
    (catch SQLException e
      (log/error (str "execute-one: Error = " (.getMessage e)))
      (let [error-data (error-state->code (.getSQLState e))]
        {:status :error
         :error-msg (.getMessage e)
         :error-name (:error error-data)
         :error-code (:value error-data)
         :sql sql
         :result nil}))))

(defn connection-ok?
  "Simple test of database connection."
  []
  (let [rslt (execute-one (sql/format {:select [["Hello" :test]]}))]
    (= (get-in rslt [:result :test]) "Hello")))

(defn truncate-table [table]
  (execute-one (sql/format {:truncate (keyword table)})))

(defn get-id
  "Retrieve a user record based on email primary key."
  [email]
  (let [sql (sql/format {:select [:*]
                         :from :auth.users
                         :where [:= :email email]})
        rslt (execute-one sql)]
    (log/debug (str "get-id: Result  = " rslt))
    (if (and (= :ok (:status rslt))
             (nil? (:result rslt)))
      {:status :error
       :error-msg (str "No ID found for " email)
       :error-name :not-found
       :error-code "-1"
       :result nil}
      rslt)))

(defn delete-id [email]
  (let [sql1 (-> (h/delete-from :auth.confirm)
                 (h/where [:= :email email])
                 (sql/format))
        sql2 (-> (h/delete-from :auth.users)
                 (h/where [:= :email email])
                 (sql/format))
        rslt1 (execute-one sql1)
        rslt2 (execute-one sql2)]
    (log/debug (str "delete-id: Result 1: " rslt1 " Result 2: " rslt2))
    rslt2))

(defn add-id
  "Create a new user record."
  [email first-name last-name password & {:keys [modified-by] :or {modified-by "system"}}]
  (let [sql (sql/format {:insert-into [:auth.users]
                         :columns [:email :first_name :last_name :password :created_by :modified_by]
                         :values [[email first-name last-name password modified-by modified-by]]})
        rslt (execute-one sql)]
    (log/debug "add-id: email = " email " Result = " rslt)
    rslt))

(defn add-confirm-record
  "Setup an account confirmation record."
  [email id & {:keys [modified-by] :or {modified-by "system"}}]
  (let [sql (sql/format {:insert-into [:auth.confirm]
                         :columns [:confirm_id :email :created_by]
                         :values [[id email modified-by]]})
        rslt (execute-one sql)]
    (log/debug "add-confirm-record: Result = " rslt)
    rslt))

(defn set-confirm-flag
  "Set is_confirmed value for `id`. The `ip` is the client IP."
  [id email ip & {:keys [modified_by] :or {modified_by "system"}}]
  (let [sql (-> (h/update :auth.confirm)
                (h/set {:is_confirmed true
                        :verified_dt :current_timestamp
                        :verified_ip ip
                        :verified_by modified_by})
                (h/where [:= :confirm_id id]
                         [:= :email email])
                (sql/format))
        rslt (execute-one sql)]
    (log/debug "set-confirm-flag: Result = " rslt)
    (if (and (= :ok (:status rslt))
             (nil? (:result rslt)))
      {:status :error
       :error-msg "No matching confirmation ID"
       :error-name :not-found
       :error-code "-1"
       :result nil}
      rslt)))

(defn get-confirm-record
  "Retrieve the confirm record for email."
  [email vid]
  (let [sql (-> (h/select :*)
                (h/from :auth.confirm)
                (h/where [:= :email email]
                         [:= :confirm_id (str vid)])
                (sql/format))
        rslt (execute-one sql)]
    rslt))

(comment)




