(ns user
  (:require [integrant.core :as ig]
            [taoensso.timbre :as log]
            [taoensso.timbre :refer [debug]]
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

(comment
  (defn my-play [v]
    (condp re-matches v
      #"^00.*$" {:status :match1 :value v}
      #"^01.*$" {:status :match2 :value v}
      #"^02.*$" {:status :match3 :value v}
      {:status :generic-match :value v}))
  (my-play "0400")
  )
