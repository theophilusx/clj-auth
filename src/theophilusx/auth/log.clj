(ns theophilusx.auth.log
  (:require [integrant.core :as ig]
            [taoensso.timbre :as log]
            [taoensso.timbre.appenders.core :refer [spit-appender]]))

(defmethod ig/init-key :theophilusx.auth.log/logger [_ {:keys [log-file]}]
  (log/debug "Enabling logg9ing")
  (log/merge-config! {:appenders {:spit (spit-appender {:fname log-file})}}))

(defmethod ig/halt-key! :theophilusx.auth.log/logger [_ _]
  (log/debug "Disabling logging")
  (log/merge-config! {:appenders {:spit {:enabled? false}}}))
