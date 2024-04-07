(ns theophilusx.auth.hooks
  (:require  [integrant.core :as ig]
             [theophilusx.auth.core :refer [config system start-system
                                            stop-system]]
             [theophilusx.auth.utils :refer [read-config]]
             [theophilusx.auth.log :as log]
             [kaocha.hierarchy :as hierarchy]))

(def standalone (atom false))

(defn init-system [cfg]
  (log/set-min-level :error)
  (when-not @config
    (let [system-cfg (merge (read-config)
                            {:theophilusx.auth.core/web-server
                             {:port  3001
                              :join? false
                              :site  {:key :theophilusx.auth.routes/site}}})]
      (reset! config system-cfg)
      (reset! system (start-system system-cfg))
      (reset! standalone true)))
  cfg)

(defn close-system [rslt]
  (stop-system @system)
  (reset! system nil)
  (when @standalone (reset! standalone false))
  rslt)


