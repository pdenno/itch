(ns owendenno.itch.client.start
  (:require [figwheel.client :as fw]
            [owendenno.itch.client.app :as app]))

;;; In rente this is in dev. I haven't gotten that to work yet. 

(enable-console-print!)

(fw/watch-and-reload
 :websocket-url "ws://localhost:3449/figwheel-ws"
 :jsload-callback #(swap! app/state update-in [:re-render-flip] not))

(app/main)
