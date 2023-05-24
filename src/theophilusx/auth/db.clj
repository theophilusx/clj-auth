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

(defmethod ig/init-key :theophilusx.auth.db/data-source [_ config]
  (let [e (read-env "resources/.env.edn")
        c (merge {:user (:db-user e) :password (:db-password e)} config)]
    (log/debug (str "data-source: Init connection pool. config = " config))
    (reset! database (connection/->pool ComboPooledDataSource c))
    c))
  
(defmethod ig/halt-key! :theophilusx.auth.db/data-source [_ _]
  (log/debug "data-source: Closing connection pool")
  (.close @database)
  (reset! database nil))

(defn error-state->code [state]
  (condp re-matches state
    #"^0100C$" {:error :warning.dynamic.result.sets.returned :value state}
    #"^01008$" {:error :warning.implicit.zero.bit.padding :value state}
    #"^01003$" {:error :warning.null.value.eliminated.in.set.function :value state}
    #"^01007$" {:error :warning.privilege.not.granted :value state}
    #"^01007$" {:error :warning.privilege.not.revoked :value state}
    #"^01004$" {:error :warning.string.data.right.truncation :value state}
    #"^01P01$" {:error :warning.deprecated.feature :value state}
    #"^02.*$" {:error :warning.no.data :value state}
    #"^03000$" {:error :warning.sql.statement.not.yet.complete :value state}
    #"^08003$" {:error :error.connection.does.not.exist :value state}
    #"^08006$" {:error :error.connection.failure :value state}
    #"^08001$" {:error :error.sqlclient.unable.to.establish.sqlconnection :value state}
    #"^08004$" {:error :error.sqlserver.rejected.establishment.of.sqlconnection :value state}
    #"^08007$" {:error :error.transaction.resolution.unknown :value state}
    #"^08P01$" {:error :error.protocol.violation :value state}
    #"^09000$" {:error :error.triggered.action.exception :value state}
    #"^0A000$" {:error :error.feature.not.supported :value state}
    #"^0B000$" {:error :error.invalid.transaction.initiation :value state}
    #"^0F.*$" {:error :error.locator.exception :value state}
    #"^0L.*$" {:error :error.invalid.grantor :value state}
    #"^0P000$" {:error :error.invlaid.role.specification :value state}
    #"^0Z.*$" {:error :error.diagnostics.exception :value state}
    #"^20.*$" {:error :error.case.not.found :value state}
    #"^21.*$" {:error :error.cardinality.violation :value state}
    #"^22000$" {:error :error.data.exception :value state}
    #"^2202E$" {:error :error.array.subscript.error :value state}
    #"^22021$" {:error :error.character.not.in.repertoire :value state}
    #"^22008$" {:error :error.datetime.field.overflow :value state}
    #"^22012$" {:error :error.division.by.zero :value state}
    #"^22005$" {:error :error.error.in.assignment :value state}
    #"^2200B$" {:error :error.escape.character.conflict :value state}
    #"^22022$" {:error :error.indicator.overflow :value state}
    #"^22015$" {:error :error.interval.field.overflow :value state}
    #"^2201E$" {:error :error.invalid.argument.for.logarithm :value state}
    #"^22014$" {:error :error.invalid.argument.for.ntile.function :value state}
    #"^22016$" {:error :error.invalid.argument.for.nth.value.function :value state}
    #"^2201F$" {:error :error.invalid.argument.for.power.function :value state}
    #"^2201G$" {:error :error.invalid.argument.for.width.bucket.function :value state}
    #"^22018$" {:error :error.invalid.character.value.for.cast :value state}
    #"^22007$" {:error :error.invalid.datetime.format :value state}
    #"^22019$" {:error :error.invalid.escape.character :value state}
    #"^2200D$" {:error :error.invalid.escape.octet :value state}
    #"^22025$" {:error :error.invalid.escape.sequence :value state}
    #"^22P06$" {:error :error.nonstandard.use.of.escape.character :value state}
    #"^22010$" {:error :error.invalid.indicator.parameter.value :value state}
    #"^22023$" {:error :error.invalid.parameter.value :value state}
    #"^2201B$" {:error :error.invalid.regular.expression :value state}
    #"^2201W$" {:error :error.invalid.row.count.in.limit.clause :value state}
    #"^2201X$" {:error :error.invalid.row.count.in.result.offset.clause :value state}
    #"^2202H$" {:error :error.invalid.tablesample.argument :value state}
    #"^2202G$" {:error :error.invalid.tablesample.repeat :value state}
    #"^22009$" {:error :error.invalid.time.zone.displacement.value :value state}
    #"^2200C$" {:error :error.invalid.use.of.escape.character :value state}
    #"^2200G$" {:error :error.most.specific.type.mismatch :value state}
    #"^22004$" {:error :error.null.value.not.allowed :value state}
    #"^22002$" {:error :error.null.value.no.indicator.parameter :value state}
    #"^22003$" {:error :error.numeric_value_out_of_range :value state}
    #"^22026$" {:error :error.string.data.length.mismatch :value state}
    #"^22001$" {:error :error.string_data_right_truncation :value state}
    #"^22011$" {:error :error.substring_error :value state}
    #"^22027$" {:error :error.trim.error :value state}
    #"^22024$" {:error :error.unterminated.c.string :value state}
    #"^2200F$" {:error :error.zero.length.character.string :value state}
    #"^22P01$" {:error :error.floating.point.exception :value state}
    #"^22P02$" {:error :error.invalid.text.representation :value state}
    #"^22P03$" {:error :error.invalid.binary.representation :value state}
    #"^22P04$" {:error :error.bad.copy.file.format :value state}
    #"^22P05$" {:error :error.untranslatable.character :value state}
    #"^2200L$" {:error :error.not.an.xml.document :value state}
    #"^2200M$" {:error :error.invalid.xml.document :value state}
    #"^2200N$" {:error :error.invalid.xml.content :value state}
    #"^2200S$" {:error :error.invalid.xml.comment :value state}
    #"^2200T$" {:error :error.invalid.xml.processing.instruction :value state}
    #"^23000$" {:error :error.integrity.constraint.violation :value state}
    #"^23001$" {:error :error.restrict.violation :value state}
    #"^23502$" {:error :error.not.null.violation :value state}
    #"^23503$" {:error :error.foreign.key.violation :value state}
    #"^23505$" {:error :error.unique.violation :value state}
    #"^23514$" {:error :error.check.violation :value state}
    #"^23P01$" {:error :error.exclusion.violation :value state}
    #"^24000$" {:error :error.invalid.cursor.state :value state}
    #"^25.*$" {:error :error.invalid.transaction.state :value state}
    #"^26.*$" {:error :error.invalid.sql.statement.name :value state}
    #"^27.*$" {:error :error.triggered.data.change.violation :value state}
    #"^28000$" {:error :error.invalid.authorization.specification :value state}
    #"^28P01$" {:error :error.invalid.password :value state}
    #"^2B.*$" {:error :error.dependent.privilege.descriptions.still.exist :value state}
    #"^2D000$" {:error :error.invalid.transaction.termination :value state}
    #"^2F.*$" {:error :error.sql.routine.exception :value state}
    #"^34000$" {:error :error.invalid.cursor.name :value state}
    #"^38.*$" {:error :error.external.routine.exception :value state}
    #"^39.*$" {:error :error.external.routine.invocation.exception :value state}
    #"^3B.*$" {:error :error.savepoint.exception :value state}
    #"^3D000$" {:error :error.invalid.catalogue.name :value state}
    #"^3F000$" {:error :error.invalid.schema.name :value state}
    #"^40.*$" {:error :error.transaction.rollback :value state}
    #"^42000$" {:error :error.syntax.error.or.access.rule.violation :value state}
    #"^42601$" {:error :error.syntax.error :value state}
    #"^42501$" {:error :error.insufficient.privilege :value state}
    #"^42846$" {:error :error.cannot.coerce :value state}
    #"^42803$" {:error :error.grouping.error :value state}
    #"^42P20$" {:error :error.windowing.error :value state}
    #"^42P19$" {:error :error.invalid.recursion :value state}
    #"^42830$" {:error :error.invalid.foreign.key :value state}
    #"^42602$" {:error :error.invalid.name :value state}
    #"^42622$" {:error :error.name.too.long :value state}
    #"^42939$" {:error :error.reserved.name :value state}
    #"^42804$" {:error :error.datatype.mismatch :value state}
    #"^42P18$" {:error :error.indeterminate.datatype :value state}
    #"^42P21$" {:error :error.collation.mismatch :value state}
    #"^42P22$" {:error :error.indeterminate.collation :value state}
    #"^42809$" {:error :error.wrong.object.type :value state}
    #"^42703$" {:error :error.undefined.column :value state}
    #"^42883$" {:error :error.undefined.function :value state}
    #"^42P01$" {:error :error.undefined.table :value state}
    #"^42P02$" {:error :error.undefined.parameter :value state}
    #"^42704$" {:error :error.undefined.object :value state}
    #"^42701$" {:error :error.duplicate.column :value state}
    #"^42P03$"	{:error :error.uplicate.cursor :value state}
    #"^42P04$" {:error :error.duplicate.database :value state}
    #"^42723$" {:error :error.duplicate.function :value state}
    #"^42P05$" {:error :error.duplicate.prepared.statement :value state}
    #"^42P06$" {:error :error.duplicate.schema :value state}
    #"^42P07$" {:error :error.duplicate.table :value state}
    #"^42712$" {:error :error.duplicate.alias :value state}
    #"^42710$" {:error :error.duplicate.object :value state}
    #"^42702$" {:error :error.ambiguous.column :value state}
    #"^42725$" {:error :error.ambiguous.function :value state}
    #"^42P08$" {:error :error.ambiguous.parameter :value state}
    #"^42P09$" {:error :error.ambiguous.alias :value state}
    #"^42P10$" {:error :error.invalid.column.reference :value state}
    #"^42611$" {:error :error.invalid.column.definition :value state}
    #"^42P11$" {:error :error.invalid.cursor.definition :value state}
    #"^42P12$" {:error :error.invalid.database.definition :value state}
    #"^42P13$"	{:error :error.invalid.function.definition :value state}
    #"^42P14$" {:error :error.invalid.prepared.statement.definition :value state}
    #"^42P15$" {:error :error.invalid.schema.definition :value state}
    #"^42P16$" {:error :error.invalid.table.definition :value state}
    #"^42P17$" {:error :error.invalid.object.definition :value state}
    #"^44000$" {:error :error.with.check.option.violation :value state}
    #"^53000$" {:error :error.insufficient.resources :value state}
    #"^53100$" {:error :error.disk.full :value state}
    #"^53200$" {:error :error.out.of.memory :value state}
    #"^53300$" {:error :error.too.many.connections :value state}
    #"^53400$" {:error :error.configuration.limit.exceeded :value state}
    #"^54000$" {:error :error.program.limit.exceeded :value state}
    #"^54001$" {:error :error.statement.too.complex :value state}
    #"^54011$" {:error :error.too.many.columns :value state}
    #"^54023$" {:error :error.too.many.arguments :value state}
    #"^55000$" {:error :error.object.not.in.prerequisite.state :value state}
    #"^55006$" {:error :error.object.in.use :value state}
    #"^55P02$" {:error :error.cant.change.runtime.param :value state}
    #"^55P03$" {:error :error.lock.not.available :value state}
    #"^55P04$" {:error :error.unsafe.new.enum.value.usage :value state}
    #"^57000$" {:error :error.operator.intervention :value state}
    #"^57014$" {:error :error.query.canceled :value state}
    #"^57P01$" {:error :error.admin.shutdown :value state}
    #"^57P02$" {:error :error.crash.shutdown :value state}
    #"^57P03$" {:error :error.cannot.connect.now :value state}
    #"^57P04$" {:error :error.database.dropped :value state}
    #"^57P05$" {:error :error.idle.session.timeout :value state}
    #"^58000$" {:error :error.system_error :value state}
    #"^58030$" {:error :error.io.error :value state}
    #"^58P01$" {:error :error.undefined.file :value state}
    #"^58P02$" {:error :error.duplicate.file :value state}
    #"^72000$" {:error :error.snapshot.too.old :value state}
    #"^F0000$" {:error :error.config.file.error :value state}
    #"^F0001$" {:error :error.lock.file.exists :value state}
    #"^HV000$" {:error :error.fdw.error :value state}
    #"^HV005$" {:error :error.fdw.column.name.not.found :value state}
    #"^HV002$" {:error :error.fdw.dynamic.parameter.value.needed :value state}
    #"^HV010$" {:error :error.fdw.function.sequence.error :value state}
    #"^HV021$" {:error :error.fdw.inconsistent.descriptor.information :value state}
    #"^HV024$" {:error :error.fdw.invalid.attribute.value :value state}
    #"^HV007$" {:error :error.fdw.invalid.column.name :value state}
    #"^HV008$" {:error :error.fdw.invalid.column.number :value state}
    #"^HV004$" {:error :error.fdw.invalid.data.type :value state}
    #"^HV006$" {:error :error.fdw.invalid.data.type.descriptors :value state}
    #"^HV091$" {:error :error.fdw.invalid.descriptor.field.identifier :value state}
    #"^HV00B$" {:error :error.fdw.invalid.handle :value state}
    #"^HV00C$" {:error :error.fdw.invalid.option.index :value state}
    #"^HV00D$" {:error :error.fdw.invalid.option.name :value state}
    #"^HV090$" {:error :error.fdw.invalid.string.length.or.buffer.length :value state}
    #"^HV00A$" {:error :error.fdw.invalid.string.format :value state}
    #"^HV009$" {:error :error.fdw.invalid.use.of.null.pointer :value state}
    #"^HV014$" {:error :error.fdw_too.many.handles :value state}
    #"^HV001$" {:error :error.fdw.out.of.memory :value state}
    #"^HV00P$" {:error :error.fdw.no.schemas :value state}
    #"^HV00J$" {:error :error.fdw.option.name.not.found :value state}
    #"^HV00K$" {:error :error.fdw.reply.handle :value state}
    #"^HV00Q$" {:error :error.fdw.schema.not.found :value state}
    #"^HV00R$" {:error :error.fdw.table.not.found :value state}
    #"^HV00L$" {:error :error.fdw.unable.to.create.execution :value state}
    #"^HV00M$" {:error :error.fdw.unable.to.create.reply :value state}
    #"^HV00N$" {:error :error.fdw.unable.to.establish.connection :value state}
    #"^P0000$" {:error :error.plpgsql.error :value state}
    #"^P0001$" {:error :error.raise.exception :value state}
    #"^P0002$" {:error :error.no.data.found :value state}
    #"^P0003$" {:error :error.too.many.rows :value state}
    #"^P0004$" {:error :error.assert.failure :value state}
    #"^XX000$" {:error :error.internal.error :value state}
    #"^XX001$" {:error :error.data.corrupted :value state}
    #"^XX002$" {:error :error.index.corrupted :value state}
    {:error (keyword (str "db.error." state)) :value state}))

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
      {:status :ok
       :result rslt})
    (catch SQLException e
      (log/debug (str "execute: Error = " (.getMessage e)))
      (let [error-data (error-state->code (.getSQLState e))]
        {:status :error
         :error-msg (.getMessage e)
         :error-name (:error error-data)
         :error-code (:value error-data)
         :result nil}))))

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
      {:status :ok
       :result rslt})
    (catch SQLException e
      (log/debug (str "execute-one: Error = " (.getMessage e)))
      (let [error-data (error-state->code (.getSQLState e))]
        {:status :error
         :error-msg (.getMessage e)
         :error-name (:error error-data)
         :error-code (:value error-data)
         :result nil}))))

(defn connection-ok?
  "Simple test of database connection."
  []
  (let [rslt (execute-one (sql/format {:select [["Hello" :test]]}))]
    (= (get-in rslt [:result :test]) "Hello")))

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
  (let [sql (sql/format {:delete-from :auth.users
                         :where [:= :email email]})
        rslt (execute-one sql)]
    (log/debug (str "delete-id: Result = " rslt))
    rslt))

(defn add-id
  "Create a new user record."
  [email first-name last-name password & {:keys [modified-by] :or {modified-by "system"}}]
  (let [sql (sql/format {:insert-into [:auth.users]
                         :columns [:email :first_name :last_name :password :created_by :modified_by]
                         :values [[email first-name last-name password modified-by modified-by]]})
        rslt (execute-one sql)]
    (log/info "add-id: email = " email " Result = " rslt)
    rslt))

(defn add-confirm-record
  "Setup an account confirmation record."
  [email id & {:keys [modified-by] :or {modified-by "system"}}]
  (let [sql (sql/format {:insert-into [:auth.confirm]
                         :columns [:confirm_id :email :created_by]
                         :values [[id email modified-by]]})
        rslt (execute-one sql)]
    (log/info "add-confirm-record: Result = " rslt)
    rslt))

(defn set-confirm-flag
  "Set is_confirmed value for `id`. The `ip` is the client IP."
  [id ip & {:keys [modified_by] :or {modified_by "system"}}]
  (let [sql (sql/format {:update :auth.confirm
                         :set {:is_confirmed true
                               :verified_dt [:current_timestamp]
                               :verified_ip ip
                               :verified_by modified_by}
                         :where [:= :confirm_id id]})]
    (execute-one sql)))

(comment
  (get-id "john@example.com")
  (add-id "bongo@ongo.com" "Bongo" "Ongo" "ongo bongo")
(sql/format {:delete-from :auth.users
                         :where [:= :email "fred"]})
  )
