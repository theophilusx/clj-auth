(ns theophilusx.auth.routes
  (:require
   [muuntaja.core :as m]
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
   [theophilusx.auth.handlers :refer [not-implemented
                                      create confirm]]
   [integrant.core :as ig]
   [taoensso.timbre :as log]))

(def routes
  ["/api" {:name "Auth API"}
   ["/create"
    {:name "Create new account"
     :post {:handler create
            :parameters {:body {:email s/Str
                                :first-name s/Str
                                :last-name s/Str
                                :password s/Str}}}
     :coercion reitit.coercion.schema/coercion}]
   ["/confirm/:user-id/:key"
    {:name "Confirm account creation request"
     :get {:parameters {:path {:user-id s/Int
                               :key s/Str}}
           :handler confirm}
     :coercion reitit.coercion.schema/coercion}]])

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
  (log/debug "site-handler: Configuring default site handlers")
  (assoc config :handler #'app))

(comment)
