(ns theophilusx.auth.utils-test
  (:require [clojure.test :refer [deftest is testing]]
            [theophilusx.auth.utils :as sut]
            [clojure.string :refer [blank? starts-with?]]
            [java-time.api :as jt]))

(deftest read-env
  (let [env-file "resources/dev-env.edn"
        env-keys #{:db-user :db-password :db-name :db-host :db-port :db-password-file
                   :db-script-path :smtp-server :smtp-port :smtp-tls :smtp-user :smtp-password
                   :smtp-from :smtp-dev-address :config-file
                   :jwt-private-key-file :jwt-public-key-file
                   :jwt-key-alg :jwt-passphrase}]
    (testing "read envrionment settings file"
      (let [ev (sut/read-env env-file)]
        (is (map? ev))
        (is (= env-keys (set (keys ev))))))
    (testing "fail to read non-existent envrionment file"
      (let [ev (sut/read-env "resources/does-not-exist.edn")]
        (is (nil? ev))))))

(deftest read-config
  (let [env-file "resources/dev-env.edn"
        env-data (sut/read-env env-file)
        cfg-keys #{:theophilusx.auth.log/logger
                   :theophilusx.auth.routes/site
                   :theophilusx.auth.core/web-server
                   :theophilusx.auth.db/data-source
                   :theophilusx.auth.mail/post
                   :theophilusx.auth.utils/key}]
    (testing "Successfully read system config"
      (let [cfg (sut/read-config env-data)]
        (is (map? cfg))
        (is (= cfg-keys (set (keys cfg))))
        (let [db-cfg (:theophilusx.auth.db/data-source cfg)
              db-keys #{:user :password :dbname
                        :dbtype :subprotocol :subname}]
          (is (map? db-cfg))
          (is (= db-keys (set (keys db-cfg)))))))
    (testing "Handle non-existent config file gracefuly"
      (let [cfg (sut/read-config {:config-file "does-not-exist"})]
        (is (nil? cfg))))))

(deftest map->str
  (let [m {:key1 "value1"
           :key2 2
           :key3 3.5
           :key4 \a}
        m-str ":key1\t\t: value1\n:key2\t\t: 2\n:key3\t\t: 3.5\n:key4\t\t: a"]
    (testing "map converted into expected string"
      (is (= m-str (sut/map->str m))))
    (testing "empty map gives empty string"
      (is (blank? (sut/map->str {}))))))

(deftest hash-pwd
  (let [p "a test password"]
    (testing "generates hash of correct length"
      (let [hp (sut/hash-pwd p)]
        (is (= 103 (count  hp)))))
    (testing "same password matches hashed version"
      (let [hp (sut/hash-pwd p)
            vfy (sut/verify-pwd p hp)]
        (is (:valid vfy))))
    (testing "different pwd does not match stored version"
      (let [hp (sut/hash-pwd p)
            vfy (sut/verify-pwd "A test password" hp)]
        (is (not (:valid vfy)))))))

(deftest jwt-sign
  (testing "Basic signing of payload"
    (let [token (sut/jwt-sign {:payuload "This is the payload"})]
      (is (not (nil? token)))
      (is (string? token))
      (is (= 410 (count token))))))

(deftest jwt-verify
  (testing "Validate basic token"
    (let [token (sut/jwt-sign {:payload "This is the payload"})
          claims (sut/jwt-verify token)]
      (println (str "Claims: " claims))
      (is (map? claims))
      (is (contains? claims :claims)))))

(deftest sing-verify
  (testing "Sign and verify claims with expiration"
    (let [validated (-> {:payload "Hello World" :exp (jt/plus (jt/instant) (jt/minutes 3))}
                        (sut/jwt-sign)
                        (sut/jwt-verify))]
      (is (map? validated))
      (is (= :ok (:status validated)))
      (is (contains? (:claims validated) :payload))
      (is (contains? (:claims validated) :exp))))
  (testing "Fail verify with token past expieration"
    (let [verified (-> {:payload "WOn't see me" :exp (jt/plus (jt/instant) (jt/minutes 2))}
                       (sut/jwt-sign)
                       (sut/jwt-verify {:now (jt/plus (jt/instant) (jt/minutes 3))}))]
      (println (str "verified: " verified))
      (is (map? verified))
      (is (= :error (:status verified)))
      (is (nil? (:claims verified)))
      (is (starts-with? (:error-msg verified) "Token is expired"))
      (is (= :exp (:error-code verified)))
      (is (= :validation (:error-name verified)))))
  (testing "Claim with valid issuer"
    (let [verified (-> {:payload "fred" :iss "example.com"}
                       (sut/jwt-sign)
                       (sut/jwt-verify {:iss ["example.com"]}))]
      (is (map? verified))
      (is (= :ok (:status verified)))
      (is (= (set (keys (:claims verified))) #{:payload :iss}))))
  (testing "Reject claim wiht invalid issuer"
    (let [verified (-> {:payload "fred" :iss "bad-place.com"}
                       (sut/jwt-sign)
                       (sut/jwt-verify {:iss ["example.com" "auth.example.com"]}))]
      (is (map? verified))
      (is (= :error (:status verified)))
      (is (nil? (:claims verified)))
      (is (starts-with? (:error-msg verified) "Issuer does not match"))
      (is (= :iss (:error-code verified)))
      (is (= :validation (:error-name verified)))))
  (testing "Verify valid audience"
    (let [verified (-> {:payload "fred" :aud ["user" "admin"]}
                       (sut/jwt-sign)
                       (sut/jwt-verify {:aud "admin"}))]
      (is (map? verified))
      (is (= :ok (:status verified)))))
  (testing "Fail verify due to bad audience"
    (let [verified (-> {:payload "fred" :aud "guest"}
                       (sut/jwt-sign)
                       (sut/jwt-verify {:aud "admin"}))]
      (is (map? verified))
      (is (= :error (:status verified)))
      (is (nil? (:clamis verified)))
      (is (starts-with? (:error-msg verified) "Audience does not match"))
      (is (= :aud (:error-code verified)))
      (is (= :validation (:error-name verified)))))
  (testing "Verify valid subject"
    (let [verified (-> {:payload "fred" :sub "subject"}
                       (sut/jwt-sign)
                       (sut/jwt-verify {:sub "subject"}))]
      (is (map? verified))
      (is (= :ok (:status verified)))))
  (testing "Fail verify due to bad subject"
    (let [verified (-> {:payload "fred" :sub "guest"}
                       (sut/jwt-sign)
                       (sut/jwt-verify {:sub "admin"}))]
      (is (map? verified))
      (is (= :error (:status verified)))
      (is (nil? (:clamis verified)))
      (is (starts-with? (:error-msg verified) "The subject does not match"))
      (is (= :sub (:error-code verified)))
      (is (= :validation (:error-name verified)))))
  (testing "Verify with not before time claim"
    (let [verified (-> {:payload "fred" :nbf (jt/instant)}
                       (sut/jwt-sign)
                       (sut/jwt-verify {:now (jt/plus (jt/instant) (jt/minutes 1))}))]
      (is (map? verified))
      (is (= :ok (:status verified)))))
  (testing "Fail verify due to bad not before time"
    (let [verified (-> {:payload "fred" :nbf (jt/instant)}
                       (sut/jwt-sign)
                       (sut/jwt-verify {:now (jt/minus (jt/instant) (jt/minutes 1))}))]
      (is (map? verified))
      (is (= :error (:status verified)))
      (is (nil? (:clamis verified)))
      (is (starts-with? (:error-msg verified) "Token is not yet valid"))
      (is (= :nbf (:error-code verified)))
      (is (= :validation (:error-name verified)))))
  (testing "Verify with issue at time claim"
    (let [verified (-> {:payload "fred" :iat (jt/instant)}
                       (sut/jwt-sign)
                       (sut/jwt-verify {:now (jt/instant) :max-age 120}))]
      (is (map? verified))
      (is (= :ok (:status verified)))))
  (testing "Fail verify due to max-age being exceeded"
    (let [verified (-> {:payload "fred" :iat (jt/instant)}
                       (sut/jwt-sign)
                       (sut/jwt-verify {:now (jt/plus (jt/instant) (jt/minutes 3))
                                        :max-age 120}))]
      (is (map? verified))
      (is (= :error (:status verified)))
      (is (nil? (:clamis verified)))
      (is (starts-with? (:error-msg verified) "Token is older than max-age"))
      (is (= :max-age (:error-code verified)))
      (is (= :validation (:error-name verified))))))

(comment
(jt/local-date-time)
(jt/plus (jt/local-date-time) (jt/seconds 2))
)
