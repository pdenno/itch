(ns owendenno.itch.server
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [compojure.core :refer [routes GET POST]]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.resource :refer (wrap-resource)]
            [org.httpkit.server :refer (run-server)]))

(def ^:private diag (atom nil))

(defn handler []
  (routes
   (GET  "/" _ (clojure.java.io/resource "index.html"))
   (route/not-found "<h1>Page not found</h1>")))

(defn app [handler]
  (let [ring-defaults-config
        (-> site-defaults
            (assoc-in
             [:security :anti-forgery]
             {:read-token (fn [req] (-> req :params :csrf-token))})
            (assoc-in [:static :resources] "public"))]
    (-> handler
        (wrap-defaults ring-defaults-config)
        (wrap-resource "/META-INF/resources"))))

(defrecord HttpServer [port server-stop]
  component/Lifecycle
  (start [component]
    (if server-stop
      component
      (let [component (component/stop component)
            handler (handler)
            server-stop (run-server (app handler) {:port port})]
        (assoc component :server-stop server-stop))))
  (stop [component]
    (if server-stop
      (server-stop)
      (log/debug "Not HTTP server on component stop!"))
    (log/debug "HTTP server stopped")
    (assoc component :server-stop nil)))

;;; This is used in system.clj:
(defn new-http-server [port]
  (let [handler (handler)
        server-stop (run-server (app handler) {:port port})]
      (log/debug "HTTP server started")
      (map->HttpServer {:port port
                        :server-stop server-stop})))
