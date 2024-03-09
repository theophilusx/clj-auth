(ns theophilusx.auth.log
  (:require [integrant.core :as ig]
            [taoensso.timbre :as tl]
            [taoensso.timbre.appenders.core :refer [spit-appender]]))

(defmethod ig/init-key :theophilusx.auth.log/logger [_ {:keys [log-file log-level]}]
  (tl/debug "Enabling logging")
  (tl/merge-config! {:min-level [["theophilusx.*" log-level]
                                 ["user" log-level]
                                 [#{"*"} :info]]
                     :appenders {:spit (spit-appender {:fname log-file})}}))

;; (defmethod ig/halt-key! :theophilusx.auth.log/logger [_ _]
;;   (log/debug "Disabling logging")
;;   (log/merge-config! {:appenders {:spit {:enabled? false}}})create)

(defn record-problem [fn-name fn-call error & {:keys [data] :or {data ""}}]
  (error (str fn-name " after calling: " fn-call " error: " error " " data)))



