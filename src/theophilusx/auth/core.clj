(ns theophilusx.auth.core
  (:require [theophilusx.auth.system :as systgem]))

(defn -main []
  (system/load-config )
  (system/start-system))

