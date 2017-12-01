(defproject owendenno/itch "0.1.0-SNAPSHOT"
  :description "A Scratch-like environment for functional programming"
  :url "http://example.com/FIXME"
  :license {:name "GPL version 2" }
  :dependencies [[org.clojure/clojure            "1.9.0-beta2"]
                 [org.clojure/clojurescript      "1.9.946"]
                 [com.stuartsierra/component     "0.3.2"]
                 [reagent                    "0.7.0"]
                 [reagent-forms              "0.5.31"]
                 [re-frame                   "0.10.2"] 
                 [re-frisk                   "0.5.0"] 
                 [org.webjars/bootstrap      "3.3.7"] ; "4.0.0-alpha" had problems with menu
                 [quil                       "2.6.0"]]
  :main ^:skip-aot owendenno.itch
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
