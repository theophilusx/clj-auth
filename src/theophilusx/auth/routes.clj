(ns theophilusx.auth.routes
  (:require [muuntaja.core :as m]
            [reitit.ring :as ring]
            [reitit.http :as http]
            [reitit.http.coercion :as coercion]
            [reitit.coercion.schema]
            [schema.core :as s]
            [reitit.interceptor.sieppari :as sieppari]
            [reitit.http.interceptors.parameters :as parameters]
            [reitit.http.interceptors.dev :as dev]
            [reitit.http.interceptors.muuntaja :as muuntaja]
            [reitit.http.interceptors.exception :as exception]
            [reitit.http.interceptors.multipart :as multipart]
            [theophilusx.auth.handlers :refer [not-implemented]]
            [integrant.core :as ig]
            [theophilusx.auth.log :as log]))

(def routes
  [["/"
    [""
     {:name "Login Form"
      :get  {:handler not-implemented}}]]
   ["/api" {:name "Auth API"}
    ["/create"
     {:name     "Create new account"
      :post     {:handler    not-implemented
                 :parameters {:body {:email      s/Str
                                     :first-name s/Str
                                     :last-name  s/Str
                                     :password   s/Str}}}
      :coercion reitit.coercion.schema/coercion}]
    ["/confirm/:user-id/:key"
     {:name     "Confirm account creation request"
      :get      {:parameters {:path {:user-id s/Int
                                     :key     s/Str}}
                 :handler    not-implemented}
      :coercion reitit.coercion.schema/coercion}]
    ["/authn"
     {:name     "Authenticate"
      :post     {:handler    not-implemented
                 :parameters {:body {:email    s/Str
                                     :password s/Str}}}
      :coercion reitit.coercion.schema/coercion}]]])

(def app
  (http/ring-handler
   (http/router
    routes
    {:reitit.interceptor/transform dev/print-context-diffs
     :data {:coercion reitit.coercion.schema/coercion
            :muuntaja m/instance
            :interceptors [(muuntaja/format-interceptor)
                           (parameters/parameters-interceptor)
                           (muuntaja/format-negotiate-interceptor)
                           (muuntaja/format-response-interceptor)
                           (exception/exception-interceptor)
                           (muuntaja/format-request-interceptor)
                           (coercion/coerce-exceptions-interceptor)
                           (coercion/coerce-response-interceptor)
                           (coercion/coerce-request-interceptor)
                           (multipart/multipart-interceptor)]}})
   (ring/create-default-handler)
   {:executor sieppari/executor}))

(defmethod ig/init-key :theophilusx.auth.routes/site [_ config]
  (log/info "Initialising web routes")
  (log/debug "site-handler: Configuring default site handlers")
  (assoc config :handler #'app))

(comment)
