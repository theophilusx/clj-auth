(ns user
  (:require [integrant.core :as ig]
            [theophilusx.auth.log :as log]
            [theophilusx.auth.core :refer [config system]]
            [theophilusx.auth.utils :refer [read-config]]
            [integrant.repl :refer [go halt reset]]
            [integrant.repl.state :as rstate]
            [lambdaisland.launchpad.watcher :refer [watch!]]))

(defn reload-config [opts]
  (println opts)
  (read-config))

(watch! {(.getCanonicalPath (clojure.java.io/file "resources/config.local.edn")) reload-config
         (.getCanonicalPath (clojure.java.io/file "resources/config.edn"))       reload-config})

(integrant.repl/set-prep! (fn []
                            (let [cfg (read-config)]
                              (ig/load-namespaces cfg)
                              (ig/prep cfg)
                              (reset! config cfg))))

(defn start-system []
(log/info "Starting system")
(go)
(reset! system rstate/system)) 

(defn stop-system []
(log/info "Halting system")
(halt)
(reset! system {}))

(defn reset-system []
(log/info "Resetting system")
(reset)
(reset! system rstate/system))

(comment
(play {:body {:email "fred@home" :first-name "Fred" :last-name "Dagg"}})
(start-system)
(let [env-data (read-env "resources/dev-env.edn")
      cfg      (read-config env-data)]
  (reset! config cfg)))



