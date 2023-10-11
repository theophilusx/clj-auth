(ns theophilusx.auth.utils
  (:require [clojure.edn :as edn]
            [clojure.string :refer [join]]
            [taoensso.timbre :as log]
            [buddy.hashers :as hashers]
            [integrant.core :as ig]))

(defn read-env
  "Load .env.edn file of envrionment specific settings."
  [env-file]
  (try
    (log/debug (str "read-env: env-file = " env-file))
    (edn/read-string (slurp env-file))
    (catch Exception e
      (log/error (str "read-env: " (.getMessage e)))
      nil)))

(defn read-config
  "Load the integrant system config file."
  [env-data]
  (try 
    (log/debug (str "Reading config file: " (:config-file env-data)))
    (let [cfg (ig/read-string (slurp (:config-file env-data)))
          db-cfg (merge  {:user (:db-user env-data)
                          :password (:db-password env-data)
                          :dbname (:db-name env-data)}
                         (:theophilusx.auth.db/data-source cfg))
          mail-cfg (merge  {:host (:smtp-server env-data)
                            :port (:smtp-port env-data)
                            :tls (:smtp-tls env-data)
                            :user (:smtp-user env-data)
                            :pass (:smtp-password env-data)
                            :dev-address (:smtp-dev-address env-data)
                            :from-address (:smtp-from env-data)}
                           (:theophilusx.auth.maail/post cfg))]
      (merge cfg {:theophilusx.auth.db/data-source db-cfg
                  :theophilusx.auth.mail/post mail-cfg})
      )
    (catch Exception e
      (log/error "read-config: " (.getMessage e))
      nil)))

(defn map->str
  "Convert a map to a string."
  [m]
  (let [s (into []
                (for [k (keys m)]
                  (str k "\t\t: " (get m k))))]
    (join "\n" s)))

(defn hash-pwd
  "Generate hash of password."
  [pwd]
  (hashers/derive (or pwd (.toString (random-uuid))) {:alg :bcrypt+blake2b-512}))

(defn verify-pwd
  "Verify supplied pwd same as recorded pwd.
  The supplied password is the plain text password while
  the recorded password is the hashed password kept in back end store.
  Returns an object with a :valid key that is true if supplied password
  is the same as stored pasword. Also includes an :update key which is true
  if the stored version of the password should be updated."
  [supplied-pwd recorded-pwd]
  (hashers/verify supplied-pwd recorded-pwd))
