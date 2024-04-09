(ns theophilusx.auth.utils
  (:require [clojure.edn :as edn]
            [clojure.string :refer [join]]
            [theophilusx.auth.log :as log]
            [buddy.core.keys :as bkeys]
            [buddy.hashers :as hashers]
            [buddy.sign.jwt :as jwt]
            [integrant.core :as ig]))

(def key-info (atom nil))

(defmethod  ig/init-key :theophilusx.auth.utils/key
  [_ {:keys [jwt-private-key-file jwt-public-key-file jwt-passphrase jwt-key-alg] :as config}]
  (log/info "Initialising JWT Keys")
  (log/debug "utils/keys: config " config)
  (let [priv-key-data (when jwt-private-key-file
                        (bkeys/private-key jwt-private-key-file jwt-passphrase))
        pub-key-data  (when jwt-public-key-file
                        (bkeys/public-key jwt-public-key-file))]
    (reset! key-info {:private-key priv-key-data
                      :public-key  pub-key-data
                      :alg         jwt-key-alg})))

(defn read-edn
  "Read an EDN data file, returning map of EDN data."
  [edn-file]
  (try
    (log/debug (str "read-edn: file = " edn-file))
    (ig/read-string (slurp edn-file))
    (catch Exception e
      (log/error (str "read-edn: " (.getMessage e)))
      nil)))

(defn read-config
  "Load the integrant system config file."
  []
  (try 
    (log/debug (str "Reading config files from resources/"))
    (let [lcfg      (read-edn "resources/config.local.edn")
          cfg       (read-edn "resources/config.edn")
          db-cfg    (merge  {:user     (:db-user lcfg)
                             :password (:db-password lcfg)
                             :dbname   (:db-name lcfg)}
                            (:theophilusx.auth.db/data-source cfg))
          mail-cfg  (merge  {:host         (:smtp-server lcfg)
                             :port         (:smtp-port lcfg)
                             :tls          (:smtp-tls lcfg)
                             :user         (:smtp-user lcfg)
                             :pass         (:smtp-password lcfg)
                             :dev-address  (:smtp-dev-address lcfg)
                             :from-address (:smtp-from lcfg)}
                            (:theophilusx.auth.mail/post cfg))
          utils-cfg {:jwt-private-key-file (:jwt-private-key-file lcfg)
                     :jwt-public-key-file  (:jwt-public-key-file lcfg)
                     :jwt-key-alg          (:jwt-key-alg lcfg)
                     :jwt-passphrase       (:jwt-passphrase lcfg)}]
      (merge cfg {:theophilusx.auth.db/data-source db-cfg
                  :theophilusx.auth.mail/post      mail-cfg
                  :theophilusx.auth.utils/key      utils-cfg}))
    (catch Exception e
      (log/error "read-config: " (.getMessage e))
      nil)))

(defn map->str
  "Convert a map to a string."
  [m]
  (let [s (vec (for [k (keys m)]
                 (str k "\t\t: " (get m k))))]
    (join "\n" s)))

(defn hash-pwd
"Generate hash of password."
[pwd]
(hashers/derive (or pwd (str (random-uuid))) {:alg :bcrypt+blake2b-512}))

(defn verify-pwd
  "Verify supplied pwd same as recorded pwd.
  The supplied password is the plain text password while
  the recorded password is the hashed password kept in back end store.
  Returns an object with a :valid key that is true if supplied password
  is the same as stored pasword. Also includes an :update key which is true
  if the stored version of the password should be updated."
  [supplied-pwd recorded-pwd]
  (hashers/verify supplied-pwd recorded-pwd))

(defn jwt-sign
([payload]
 (jwt-sign payload {}))
([payload opts]
 (try 
   (jwt/sign payload (:private-key @key-info) (merge opts {:alg (:alg @key-info)}))
   (catch Exception e
     (log/error (str "jwt-sign: " (.getMessage e)))
     nil))))

(defn jwt-verify
  ([token]
   (jwt-verify token {}))
  ([token opts]
   (try
     (log/debug "jwt-verify: token = " token)
     (log/debug "jwt-verify: Opts = " opts)
     {:claims (jwt/unsign token (or (:public-key @key-info)
                                    (:private-key @key-info))
                          (merge opts {:alg (:alg @key-info)}))
      :status :ok}
     (catch Exception e
       (let [e-data (ex-data e)]
         {:status     :error
          :error-msg  (.getMessage e)
          :error-name (:type e-data)
          :error-code (:cause e-data)
          :claims     nil})))))

(comment
  (let [e-data ]))
