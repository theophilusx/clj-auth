(ns user
  (:require [integrant.core :as ig]
            [taoensso.timbre :as log]
            [theophilusx.auth.core :refer [read-config config system]]
            [theophilusx.auth.utils :refer [read-env]]
            [integrant.repl :refer [go halt reset]]
            [integrant.repl.state :as rstate]))

(integrant.repl/set-prep! (fn []
                            (let [e (read-env "resources/.env.edn")
                                  cfg (read-config (:config-file e))]
                              (ig/load-namespaces cfg)
                              (ig/prep cfg)
                              (reset! config cfg))))

(defn start-system []
  (log/debug "Starting system")
  (go)
  (reset! system rstate/system))

(defn stop-system []
  (log/debug "Halting system")
  (halt)
  (reset! system {}))

(defn reset-system []
  (log/debug "Resetting system")
  (reset)
  (reset! system rstate/system))

(comment
  (start-system))



