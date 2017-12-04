(defproject owendenno/itch "0.1.0-SNAPSHOT"
  :description "A Scratch-like environment for functional programming"
  :url "http://example.com/FIXME"
  :license {:name "GPL version 2" }
  :dependencies [[org.clojure/clojure            "1.9.0-beta2"]
;;;                 [org.clojure/clojurescript      "1.9.946"]
                 [org.clojure/tools.logging      "0.4.0"]
                 [com.stuartsierra/component     "0.3.2"]
                 [environ                        "1.1.0"]

;;;                 [ring                       "1.6.3"]
;;;                 [ring/ring-defaults         "0.3.1"]
;;;                 [compojure                  "1.6.0"]
;;;                 [http-kit                   "2.2.0"]
                 
;;;                 [reagent                    "0.7.0"]
;;;                 [reagent-forms              "0.5.32"]
;;;                 [re-frame                   "0.10.2"] 
;;;                 [re-frisk                   "0.5.2"]
;;;                 [org.webjars/bootstrap      "3.3.7"]
                 [quil                       "2.6.0"]]

  
  #_:plugins #_[[lein-figwheel "0.5.14"]
                [lein-cljsbuild "1.1.4"]]
  #_:hooks #_[leiningen.cljsbuild]
  
  :source-paths ["src"]
  :resource-paths ["resources" "resources-index/prod"]
  :target-path "target/%s"

  :main ^:skip-aot owendenno.itch.client.scripts

;;; :main ^:skip-aot owendenno.itch.run
  
  
  #_:cljsbuild
  #_{:builds
   {:client {:source-paths ["src/owendenno/itch/client"]
             :figwheel true
             :compiler
             {:output-to "resources/public/js/app.js"
              :output-dir "dev-resources/public/js/out"}}}}
  
  #_:profiles #_{:dev-config {}
             ;; There is a user.clj in dev/. By design of clojure, it gets loaded if it on the path...
             :dev [:dev-config ; This pattern of use from rente. 
                   {:dependencies [[org.clojure/tools.namespace "0.2.10"]
                                   [com.cemerick/piggieback "0.2.2"] 
                                   [figwheel "0.5.14"]
                                   [figwheel-sidecar "0.5.14"]]
                    :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                    :plugins [[lein-figwheel "0.5.14"]
                              [lein-environ "1.0.1"]]
                              
                    :source-paths ["dev"] ;...now it is on the path. 
                    :resource-paths ^:replace
                    ["resources" "dev-resources" "resources-index/dev"]

                    :cljsbuild
                    {:builds
                     {:client {:source-paths ["dev"]
                               :compiler
                               {:optimizations :none
                                :source-map true}}}}

                    :figwheel {:http-server-root "public"
                               :server-port 3449
                               :repl false
                               :css-dirs ["resources/public/css"]}}]

             :prod {:cljsbuild
                    {:builds 
                     {:client {:compiler {:optimizations :advanced
                                          :elide-asserts true
                                          :pretty-print false}}}}}}
  #_:aliases  #_{"start-repl" ["do" "clean," "cljsbuild" "once," "repl" ":headless"]
                 "start"      ["do" "clean," "cljsbuild" "once," "run"]
                 "package"    ["with-profile" "prod" "do" "clean" ["cljsbuild" "once"]]})



