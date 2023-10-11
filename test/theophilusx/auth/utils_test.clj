(ns theophilusx.auth.utils-test
  (:require [clojure.test :refer [deftest is testing]]
            [theophilusx.auth.utils :as sut]
            [clojure.string :refer [blank?]]))

(deftest read-env
  (let [env-file "resources/dev-env.edn"
        env-keys #{:db-user :db-password :db-name :db-host :db-port :db-password-file
                   :db-script-path :smtp-server :smtp-port :smtp-tls :smtp-user :smtp-password
                   :smtp-from :smtp-dev-address :config-file}]
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
                   :theophilusx.auth.mail/post}]
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
