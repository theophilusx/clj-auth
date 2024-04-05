(ns theophilusx.auth.mail
  (:require [postal.core :as post]
            [integrant.core :as ig]
            [hiccup.page :refer [html5]]
            [theophilusx.auth.log :as log]))

(def smtp-server (atom {}))

(def mail-environment (atom :dev))

(def smtp-from (atom nil))

(def dev-address (atom "no-reply@bogus.site"))

(defmethod ig/init-key :theophilusx.auth.mail/post [_ config]
  (let [svr {:host (:host config)
             :port (:port config)
             :tls (:tls config)
             :user (:user config)
             :pass (:pass config)}]
    (when (= :prod (:env config))
      (reset! mail-environment :prod))
    (reset! dev-address (:dev-address config))
    (reset! smtp-from (:from-address config))
    (reset! smtp-server svr)
    config))

(defn confirm-msg
  "Create a confirmation message map with supplied confirm link."
  [link]
  {:body [:alternative
          {:type "text/plain"
           :content (str "\tVerify Account\n\t==============\n\n"
                         "Plase confirm your account by going to the below link\n\n\t"
                         link)}
          {:type "text/html"
           :content (html5 [:body
                            [:h1 "Verify Your Account"]
                            [:p "Please verify your account by going to the below link"]
                            [:a {:href link} link]])}]})

(defn send-confirm-msg 
  "send confirm email with confirm link to email address."
  [email link]
  (try 
    (let [to   (if (= :prod @mail-environment) ; only send to email if in production
                 email
                 @dev-address)
          rslt (post/send-message @smtp-server
                                  (merge (confirm-msg link)
                                         {:from    @smtp-from
                                          :to      to
                                          :subject "verify account"}))]
      (log/debug "send-confirm-msg: result = " rslt)
      (when (not= :SUCCESS (:error rslt))
        (throw (ex-info (str "Failed to send email to " email)
                        rslt)))
      :success)
    (catch Exception e
      (log/error (str "send-confirm-msg: " (ex-message e)))
      (throw (ex-info "Filed to send confirm message"
                      {:email email
                       :link  link} e)))))

(defn password-recovery-msg 
  "Generate password recovery message."
  [link]
  {:body [:alternative
          {:type "text/plain"
           :content (str "\tPassword Recovery\n\t=================\n\n"
                         "A password recovery request has been recieved for your account."
                         "If you did not make this request, feel free to ignore this message.\n\n"
                         "To reset your password, please go to the following link\n\n"
                         "\t" link)}
          {:type "text/html"
           :content (html5 [:body
                            [:h1 "Password Recovery"]
                            [:p (str "A password recovery request has been received for your account."
                                     "If you did not make this request, feel free to ignore this message.")]
                            [:p (str "To reset your password, please go to the following link")]
                            [:a {:href link} link]])}]})

(defn send-password-recovery-msg
  "Send a password recovery message to email with recovery link."
  [email link]
  (try 
    (let [to   (if (= :prod @mail-environment)
                 email
                 @dev-address)
          rslt (post/send-message
                @smtp-server
                (merge (password-recovery-msg link)
                       {:from    @smtp-from
                        :to      to
                        :subject "Password Recovery"}))]
      (when (not= :SUCCESS (:error rslt))
        (throw (ex-info (str "Failed to send email to " email)
                        {:email email
                         :link  link})))
      :success)
    (catch Exception e
      (log/error (str "send-password-recovery-msg: " (ex-message e)))
      (throw (ex-info (str "send-password-recovery-msg: " (ex-message e))
                      {:email email
                       :link  link} e)))))

(comment
  @smtp-from
  @smtp-server
  (post/send-message @smtp-server
                     {:from    "\"Auth System\" <theophilusx@gmail.com>"
                      :to      "blind-bat@hotmail.com"
                      :subject "A Second try"
                      :body    [:alternative
                                {:type    "text/plain"
                                 :content (str "\tA Test\n\t======\n\nThis is just a test")}
                                {:type    "text/html"
                                 :content (html5 [:body
                                                  [:h1 "A Test"]
                                                  [:p "This is just a test in HTML"]
                                                  [:a {:href "http://localhost:3000/api/44"} "Verify Account"]])}]})
  (post/send-message @smtp-server
                     {:from    "theophilusx@gmail.com"
                      :to      "blind-bat@hotmail.com"
                      :subject "A first test"
                      :body    "This is the first test"}))
