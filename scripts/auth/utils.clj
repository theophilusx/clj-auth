(ns auth.utils
  (:require [clojure.edn :as edn]
            [babashka.fs :as fs]))

(defn get-config [edn-file]
  (edn/read-string (slurp edn-file)))

(defn get-file-list [path glob]
  (sort (map #(fs/file-name %1) (fs/list-dir path glob))))

