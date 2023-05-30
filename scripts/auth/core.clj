(ns auth.core
  (:require [auth.utils :as utils]
            [babashka.process :refer [shell]]
            [babashka.fs :as fs]))

(defn make-pgpasswd [config-file]
  (let [config (utils/get-config config-file)]
    (spit (:db-password-file config) (str (:db-host config) ":"
                                          (:db-port config) ":"
                                          (:db-name config) ":"
                                          (:db-user config) ":"
                                          (:db-password config)))
    (shell (str "chmod 600 " (:db-password-file config)))))

(defn run-db-scripts [config scripts]
  (doseq [s scripts]
    (let [cmd (str "/usr/bin/psql -U " (:db-user config)
                    " -h " (:db-host config)
                    " -p " (:db-port config)
                    " -f ./" (:db-script-path config) "/" s
                    " " (:db-name config))]
      (shell {:extra-env {"PGPASSFILE" (:db-password-file config)}} cmd))))

(defn build-db [config-file]
  (let [config (utils/get-config config-file)
        files (utils/get-file-list (:db-script-path config) "*.up.sql")]
    (run-db-scripts config files)))

(defn destroy-db [config-file]
  (let [config (utils/get-config config-file)
        files (reverse (utils/get-file-list (:db-script-path config) "*.down.sql"))]
    (run-db-scripts config files)))
