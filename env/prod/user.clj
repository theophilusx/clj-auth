(ns user
  (:require [theophilusx.auth.system :refer [load-config]]))

(load-config "resources/prod-config.edn")
