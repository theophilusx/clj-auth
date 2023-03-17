(ns theophilusx.auth.handlers
  (:require [integrant.core :as ig]
            [muuntaja.core :as m]
            [reitit.ring :as ring]
            [reitit.coercion.schema]
            [reitit.ring.coercion :refer [coerce-exceptions-middleware
                                          coerce-request-middleware
                                          coerce-response-middleware]]
            [reitit.dev.pretty :as pretty]
            [reitit.ring.middleware.muuntaja :refer [format-middleware
                                                     format-negotiate-middleware
                                                     format-request-middleware
                                                     format-response-middleware]]
            [reitit.ring.middleware.exception :refer [exception-middleware]]
            [hiccup.core :refer [html]]
            [schema.core :as s]))

(defn map->str [m]
  (let [s (into []
                (for [k (keys m)]
                  (str k "\t\t: " (get m k))))]
    (clojure.string/join "\n" s)))

(defn not-implemented [req]
  {:status 501 :body (str "Not yet implemented\n"
                          "Page " (:uri req) " functionality not yet implemented.\n"
                          (map->str))})

(defn authn-get-handler [req]
  {:status 200
   :body {:title "Authn Request"
          :desc "An authn get handler"
          :request "Will go here!"}})

(defn authn-post-handler [req]
  {:status 200
   :body (merge {:title "Authn Request"
                 :desc "An authn post handler"
                 :request-keys (str "Keys: " (keys req))
                 :parameters (str (:parameters req))
                 :body-params (str (:body-params req))
                 :uri (:uri req)}
                (:body-params req))})

(defn authz-handler [req]
  {:status 200
   :content-type "text/plain"
   :body (str "AuthZ Handler\nAuthN Handler\nLogin Post Request\n"
              (map->str req))})

(def routes [["/api"
              ["/authn" {:name ::authn
                         :get authn-get-handler
                         :post {:parameters {:body {:username s/Str
                                                    :password s/Str}}
                                :handler authn-post-handler}}]
              ["/authz" {:name ::authz
                         :get authz-handler}]
              ["/create" {:name ::create
                          :post not-implemented}]
              ["/update" {:name ::update
                          :post not-implemented}]
              ["/delete" {:name ::delete
                          :post not-implemented}]]])

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
