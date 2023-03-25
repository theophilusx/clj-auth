(ns theophilusx.auth.migrate
  (:require [migratus.core :as migratus]
            [integrant.core :as ig]
            [taoensso.timbre :as log]))

(defmethod ig/init-key :theophilusx.auth.migrate/migration [_ config]
  (log/debug (str "migration: Migration config: " config))
  (migratus/migrate config)
  config)

(defmethod ig/halt-key! :theophilusx.auth.migrate/migration [_ {:keys [rollback?] :as config}]
  (log/debug "migration: config = " config)
  (log/debug "migration: rollback? " rollback?)
  (when rollback?
    (log/debug "migration: Migration rollback")
    (migratus/rollback config)))

