(defproject retrospector "0.1.0-SNAPSHOT"
  :description "Space Hackathon 2014"
  :source-paths ["src/clj" "src/cljs"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138"]
                 
                 ;; Web server
                 [ring "1.2.0"]
                 [compojure "1.1.5"]
                 [enlive "1.1.1"]

                 ;; json
                 [cheshire "5.2.0"]

                 ;; cljs
                 [prismatic/dommy "0.1.1"]
                 [com.cemerick/clojurescript.test "0.3.1"]]
  :profiles {:dev {:plugins [[com.cemerick/austin "0.1.4"]
                             [lein-cljsbuild "1.0.1"]]
                   :cljsbuild {:builds [{:source-paths ["src/cljs"]
                                         :compiler {:output-dir "resources/public/scripts"
                                                    :output-to "resources/public/scripts/app.js"
                                                    :optimizations :simple
                                                    :pretty-print true
                                                    :source-map "resources/public/scripts/app.js.map"}}]}}})
