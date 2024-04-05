(ns theophilusx.auth.mail-test
  (:require [clojure.test :refer [deftest is are testing]]
            [theophilusx.auth.mail :as sut]))

(deftest config-tests
  (testing "Checking namespace defs set"
    (is (map? @sut/smtp-server))
    (is (and (contains? @sut/smtp-server :host)
             (string? (:host @sut/smtp-server))))
    (is (and (contains? @sut/smtp-server :port)
             (int? (:port @sut/smtp-server))))
    (is (and (contains? @sut/smtp-server :tls)
             (boolean? (:tls @sut/smtp-server))))
    (is (and (contains? @sut/smtp-server :user)
             (string? (:user @sut/smtp-server))))
    (is (and (contains? @sut/smtp-server :pass)
             (string? (:pass @sut/smtp-server))))
    (is (and (keyword? @sut/mail-environment)
             (= :dev @sut/mail-environment)))
    (is (and (string? @sut/dev-address)
             (= "\"Auth Dev Address\" <blind-bat@hotmail.com>" @sut/dev-address)))
    (is (and (string? @sut/smtp-from)
             (= "\"Authentication System\" <theophilusx@gmail.com" @sut/smtp-from)))))

(deftest confirm-msg-tests
  (testing "Different links"
    (let [link "http://example.com/msg"
          msg (sut/confirm-msg link)]
      (is (map? msg))
      (is (contains? msg :body))
      (is (vector? (:body msg)))
      (is (= (first (:body msg)) :alternative))
      (let [a1 (nth (:body msg) 1)
            a2 (nth (:body msg) 2)
            rx (re-pattern link)]
        (is (map? a1))
        (is (map? a2))
        (is (= (:type a1) "text/plain"))
        (is (= (:type a2) "text/html"))
        (is (re-find rx (:content a1)))
        (is (re-find rx (:content a2)))))))

(deftest message-format-test
  (testing "Message format tests"
    (are [msg link] (let [a1 (nth (:body msg) 1)
                          a2 (nth (:body msg) 2)
                          rx (re-pattern link)]
                      (is (map? msg))
                      (is (contains? msg :body))
                      (is (vector? (:body msg)))
                      (is (= (first (:body msg)) :alternative))
                      (is (map? a1))
                      (is (map? a2))
                      (is (= (:type a1) "text/plain"))
                      (is (= (:type a2) "text/html"))
                      (is (re-find rx (:content a1)))
                      (is (re-find rx (:content a2))))
      (sut/confirm-msg "http://example1.com/confirm") "http://example1.com/confirm"
      (sut/password-recovery-msg "http://example2.com/recovery") "http://example2.com/recovery")))

