(ns theophilusx.auth.core
  (:require [theophilusx.auth.utils :refer [read-config]]
            [taoensso.timbre :as log]
            [integrant.core :as ig]
            [ring.adapter.jetty :refer [run-jetty]]))

(defmethod ig/init-key :theophilusx.auth.core/web-server [_ {:keys [site] :as opts}]
  (log/debug "Starting web server on port " (:port opts))
  (log/debug "Opts = " opts)
  (run-jetty (:handler site) (dissoc opts :site)))

(defmethod ig/halt-key! :theophilusx.auth.core/web-server [_ server]
  (log/debug "Shutting down web server")
  (.stop server))

(def config (atom nil))
(def system (atom nil))

(defn start-system [config]
  (log/info "Starting system")
  (ig/init config))

(defn stop-system [system]
  (log/info "Stopping system")
  (ig/halt! system))

(defn -main []
  (let [cfg (read-config)]
    (reset! config cfg)
    (reset! system (start-system cfg))))

(comment
  (-main)
  (stop-system @system)
  (log/debug @system))
