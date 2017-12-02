(ns owendenno.itch.app
  (:require [clojure.pprint :refer (pprint)]
            [clojure.spec.alpha :as s]
            [com.stuartsierra.component :as component]))

(def app-data nil)

;;; Start and stop will reset the component to what is returned here. Thus I use atoms
;;; for things that I want to change and I do not reload this file on reset.
;;; See use of (nsp/disable-reload! (find-ns 'gov.nist.sinet.app)) in util.clj.
(defrecord App []
  component/Lifecycle
  (start [component]
    component)
  (stop [component]
    component))

(defn new-app []
  (map->App {}))


