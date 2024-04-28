(ns theophilusx.auth.template
  (:require [clojure.edn :as edn]
            [hiccup2.core :as h]
            [hiccup.page :as hp]
            [theophilusx.auth.log :as log]))

(def template-root "./resources/templates")
(def default-head "[:head
                   [:meta {:charset \"utf-8\"}]
                   [:meta {:name    \"viewport\"
                           :content \"width=device-width, initial-scale=1\"}]
                   [:meta {:http-equiv \"Content-Type\"
                           :content \"text/html\"}]
                   [:title #tmpl/val :title]
                   [:link {:rel  \"stylesheet\"
                           :href \"defualt.css\"}]]
                   [:script {:src \"https://unpkg.com/htmx.org@1.9.11\"}]")

(defn parse-template
  ([template]
   (parse-template template {}))
  ([template vars]
   (try 
     (let [t-vars  (atom (or vars {}))
           data    (slurp (str template-root "/" template))
           def-var (partial (fn [t-vmap v-map]
                              (swap! t-vmap merge v-map))
                            t-vars)
           get-var (partial (fn [t-vmap tag]
                              (get @t-vmap tag "MISSING_TEMPLATE_VALUE"))
                            t-vars)
           readers {:readers {'tmpl/def def-var
                              'tmpl/val get-var}}
           edn     (edn/read-string readers data)
           hup     (if (= 2 (count edn))
                     (second edn)
                     (first edn))]
       (if (not (contains? hup :head))
         (merge hup {:head (edn/read-string readers default-head)})
         hup))
     (catch Exception e
       (let [msg (str "Failed to parse template " template)]
         (log/error msg e)
         (throw (ex-info msg {:template template :vars vars} e)))))))

(defn render-template
  ([template]
   (render-template template {}))
  ([template vars]
   (try
     (let [hup (parse-template template vars)]
       (hp/html5 {:lang "en"} (:head hup) (:body hup)))
     (catch Exception e
       (let [msg (str "Failed to render template " template)]
         (log/error msg e)
         (throw (ex-info msg {:template template :vars vars} e)))))))

(comment
  (parse-template "test2.edn") 
  (render-template "home.edn") 
  ()
  ) 
