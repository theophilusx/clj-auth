(ns user
  (:require [integrant.core :as ig]
            [taoensso.timbre :refer [debug]]
            [theophilusx.auth.system :refer [load-config config system]]
            [integrant.repl :refer [go halt reset]]
            [integrant.repl.state :as rstate]))

(integrant.repl/set-prep! (fn []
                            (load-config "resources/dev-config.edn")
                            (ig/load-namespaces @config)
                            (ig/prep @config)))

(defn start-system []
  (debug "Starting system")
  (go)
  (reset! system rstate/system))

(defn stop-system []
  (debug "Halting system")
  (halt)
  (reset! system {}))

(defn reset-system []
  (debug "Resetting system")
  (reset)
  (reset! system rstate/system))
