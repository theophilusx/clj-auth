(ns theophilusx.auth.system
  (:require [integrant.core :as ig]
            [ring.adapter.jetty :refer [run-jetty]]
            [next.jdbc :as jdbc]
            [migratus.core :as migratus]
            [taoensso.timbre :refer [debug info merge-config!]]
            [taoensso.timbre.appenders.core :refer [spit-appender]]
            [theophilusx.auth.handlers :refer [app]])
  (:import [java.sql Connection SQLException]))

(def config
  "The integrant system config map"
  (atom {}))

(def system
  "The integrant system definition."
  (atom {}))

(defmethod ig/init-key :theophilusx.auth.system/logging [_ {:keys [log-file]}]
  (merge-config! {:appenders {:spit (spit-appender {:fname log-file})}}))

(defmethod ig/halt-key! :theophilusx.auth.system/logging [_ _]
  (merge-config! {:appenders {:spit {:enabled? false}}}))

(defmethod ig/init-key :theophilusx.auth.system/site-handler [_ _]
  (debug "Configuring default site handlers")
  #'app)

(defmethod ig/init-key :theophilusx.auth.system/web-server [_ {:keys [handler] :as opts}]
  (debug "Starting web server on port " (:port opts))
  (run-jetty handler (dissoc opts :handler)))

(defmethod ig/halt-key! :theophilusx.auth.system/web-server [_ server]
  (debug "Shutting down web server")
  (.stop server))

(defmethod ig/init-key :theophilusx.auth.system/database [_ db]
  (debug (str "Database config: " db))
  (debug "Creating DB connection")
  (jdbc/get-datasource db))

(defmethod ig/init-key :theophilusx.auth.system/migration[_ config]
  (let [c (assoc-in config [:db :connection] (jdbc/get-connection (:data-source config)))]
    (debug "Migration Init")
    (debug (str "Migratus Config: " c))
    (migratus/migrate c)
    config))

(defmethod ig/halt-key! :theophilusx.auth.system/migration [_ {:keys [rollback?] :as config}]
  (when rollback?
    (debug "Migration rollback")
    (let [c (assoc-in config [:db :connection] (jdbc/get-connection (:data-source config)))]
      (migratus/rollback c))))

(defn load-config
  "Load the integrant system config file."
  [config-file]
  (debug (str "Reading config file: " config-file))
  (reset! config (ig/read-string (slurp config-file))))

(defn start-system []
  (info "Starting system")
  (reset! system (ig/init @config)))

(defn stop-system []
  (info "Stopping system")
  (ig/halt! @system)
  (reset! system {}))


