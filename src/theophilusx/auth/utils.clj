(ns theophilusx.auth.utils
  (:require [clojure.edn :as edn]
            [clojure.string :refer [join]]
            [taoensso.timbre :as log]
            [buddy.core.keys :as bkeys]
            [buddy.hashers :as hashers]
            [buddy.sign.jwt :as jwt]
            [integrant.core :as ig]))

(def key-info (atom nil))

(defmethod  ig/init-key :theophilusx.auth.utils/key
  [_ {:keys [jwt-private-key-file jwt-public-key-file
             jwt-passphrase jwt-key-alg] :as config}]
  (log/debug "utils/keys: config " config)
  (let [priv-key-data (if jwt-private-key-file
                        (bkeys/private-key jwt-private-key-file jwt-passphrase))
        pub-key-data (if jwt-public-key-file
                       (bkeys/public-key jwt-public-key-file))]
    (reset! key-info {:private-key priv-key-data
                      :public-key pub-key-data
                      :alg jwt-key-alg})))

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
                           (:theophilusx.auth.mail/post cfg))
          utils-cfg {:jwt-private-key-file (:jwt-private-key-file env-data)
                     :jwt-public-key-file (:jwt-public-key-file env-data)
                     :jwt-key-alg (:jwt-key-alg env-data)
                     :jwt-passphrase (:jwt-passphrase env-data)}]
      (merge cfg {:theophilusx.auth.db/data-source db-cfg
                  :theophilusx.auth.mail/post mail-cfg
                  :theophilusx.auth.utils/key utils-cfg}))
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
         {:status :error
          :error-msg (.getMessage e)
          :error-name (:type e-data)
          :error-code (:cause e-data)
          :claims nil})))))

(comment
  (let [e-data ]))
