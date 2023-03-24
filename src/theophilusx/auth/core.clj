(ns theophilusx.auth.core
  (:require [theophilusx.auth.utils :refer [read-env]]
            [taoensso.timbre :as log]
            [integrant.core :as ig]
            [ring.adapter.jetty :refer [run-jetty]]))

(defmethod ig/init-key :theophilusx.auth.core/web-server [_ {:keys [handler] :as opts}]
  (log/debugf "Starting web server on port " (:port opts))
  (run-jetty handler (dissoc opts :handler)))

(defmethod ig/halt-key! :theophilusx.auth.core/web-server [_ server]
  (log/debug "Shutting down web server")
  (.stop server))

(def config (atom nil))
(def system (atom nil))

(defn read-config
  "Load the integrant system config file."
  [config-file]
  (log/debug (str "Reading config file: " config-file))
  (ig/read-string (slurp config-file)))

(defn start-system [config]
  (log/info "Starting system")
  (ig/init config))

(defn stop-system [system]
  (log/info "Stopping system")
  (ig/halt! system))

(defn -main []
  (let [e (read-env "resources/.env.edn")
        cfg (read-config (:config-file e))]
    (reset! config cfg)
    (reset! system (start-system cfg))))

(comment
  )
