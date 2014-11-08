(ns retrospector.server
  (:use [retrospector.dev :only [reset-brepl-env!
                                connect-to-brepl
                                get-repl-client-js]])
  (:require [cemerick.austin.repls :refer (browser-connected-repl-js)]
            [net.cgrand.enlive-html :as enlive]
            [compojure.route :refer (resources)]
            [compojure.core :refer (GET defroutes)]
            [compojure.handler :as ch]
            ring.adapter.jetty
            [retrospector.api :refer [stars-handler]]))


(defonce server (atom nil))

(def project-root
  (str (System/getProperty "user.dir") "/resources/public"))

(def source-root
  (str (System/getProperty "user.dir") "/*"))

(enlive/deftemplate homepage "public/homepage.html" []
  [:head] (enlive/append
           (enlive/html [:link {:rel "stylesheet"
                                :type "text/css"
                                :href "/static/styles/master.css"}])))

(enlive/deftemplate app "public/app.html"
  []
  [:body] (enlive/append
           (enlive/html [:link {:rel "stylesheet"
                                :type "text/css"
                                :href "/static/styles/master.css"}])
           (enlive/html [:script (get-repl-client-js)])
           (enlive/html [:script (browser-connected-repl-js)])))

(defn source-files []
  #(slurp (:uri %)))

(defroutes site
  (resources "/static")
  (GET "/app" req (app))
  (GET "/api/v1/stars" req stars-handler)
  ;; TODO disable based on env
  ;; HACK for serving source maps via the server
  (GET source-root req (source-files))
  ;;(GET "/*" req (homepage))
  )

(defn restart-server! [& [options]]
  (when-not (nil? @server)
    ;; Close the server
    (@server))
  (let [options (merge {:port 9000 :join? false} options) 
        srv (ring.adapter.jetty/run-jetty (-> #'site ch/api) options)]
    (connect-to-brepl (reset-brepl-env!))
    (reset! server #(.stop srv))))
