(ns owendenno.itch.system
  (:require [com.stuartsierra.component :as component]
            [owendenno.itch.app :as app]
            [owendenno.itch.server :as server]
            [owendenno.itch.app :as app]))

(defn system [config]
  (let []
    (component/system-map
     :http-server (component/using (server/new-http-server (:port config)) [])
     :app (component/using (app/new-app) []))))
