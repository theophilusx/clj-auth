(ns theophilusx.auth.setup
  (:require  [integrant.core :as ig]
             [theophilusx.auth.core :refer [config system start-system]]
             [theophilusx.auth.utils :refer [read-config]]))

(defn init-system [cfg]
  (let [system-cfg (merge (read-config) {:theophilusx.auth.log/logger      {:log-file  "/tmp/auth-dev.log"
                                                                            :log-level :error}
                                         :theophilusx.auth.core/web-server {:port  3001
                                                                            :join? false
                                                                            :site  {:key :theophilusx.auth.routes/site}}})]
    (reset! config system-cfg)
    (reset! system (start-system system-cfg))
    cfg))
