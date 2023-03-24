(ns theophilusx.auth.utils
  (:require [clojure.edn :as edn]
            [clojure.string :refer [join]]
            [taoensso.timbre :as log]))

(defn read-env 
  "Load .env.edn file of envrionment specific settings."
  [env-file]
  (log/debug (str "read-env: env-file = " env-file))
  (edn/read-string (slurp env-file)))

(defn map->str [m]
  (let [s (into []
                (for [k (keys m)]
                  (str k "\t\t: " (get m k))))]
    (join "\n" s)))
