(ns theophilusx.auth.routes
  (:require [muuntaja.core :as m]
            [reitit.ring :as ring]
            [reitit.coercion.schema]
            [reitit.ring.coercion :refer [coerce-exceptions-middleware
                                          coerce-request-middleware
                                          coerce-response-middleware]]
            [reitit.ring.middleware.muuntaja :refer [format-negotiate-middleware
                                                     format-request-middleware
                                                     format-response-middleware]]
            [reitit.ring.middleware.exception :refer [exception-middleware]]
            [schema.core :as s]
            [theophilusx.auth.handlers :refer [authn-post-handler
                                               not-implemented-handler
                                               register-handler
                                               confirm-handler]]
            [integrant.core :as ig]
            [taoensso.timbre :as log]))

(def routes [["/" {:name ::root
                   :get not-implemented-handler
                   :post not-implemented-handler}]
             ["/api"
              ["/confirm/:email/:id" {:name ::confirm
                                      :get confirm-handler}]
              ["/recover/:id" {:name ::recover
                               :get not-implemented-handler
                               :post {:parameters {:body {:password s/Str
                                                          :confirm s/Str}}
                                      :handler not-implemented-handler}}]
              ["/authn" {:name ::authn
                         :get not-implemented-handler
                         :post {:parameters {:body {:username s/Str
                                                    :password s/Str}}
                                :handler authn-post-handler}}]
              ["/authz" {:name ::authz
                         :get not-implemented-handler}]
              ["/register" {:name ::register
                            :post {:parameters {:body {:email s/Str
                                                       :first-name s/Str
                                                       :last-name s/Str
                                                       :password s/Str}}
                                   :handler register-handler}}]
              ["/update" {:name ::update
                          :post not-implemented-handler}]
              ["/delete" {:name ::delete
                          :post not-implemented-handler}]]])

(def app (ring/ring-handler
          (ring/router
           routes
           {:data {:coercion reitit.coercion.schema/coercion
                   :muuntaja m/instance
                   :middleware [format-negotiate-middleware
                                format-response-middleware
                                exception-middleware
                                format-request-middleware
                                coerce-exceptions-middleware
                                coerce-request-middleware
                                coerce-response-middleware]}})))

(defmethod ig/init-key :theophilusx.auth.routes/site [_ config]
  (log/debug "site-handler: Configuring default site handlers")
  (assoc config :handler #'app))

(comment
  (app {:request-method :get
        :uri "/"}))
