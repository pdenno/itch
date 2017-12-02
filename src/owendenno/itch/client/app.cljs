(ns owendenno.itch.client.app
    (:require-macros [cljs.core.async.macros :refer [go-loop]])
    (:require [reagent.core :as reagent]
              [re-frisk.core :refer [enable-re-frisk!]]
              [re-frame.core :as rf]
              [owendenno.itch.client.views :as views]))

(defonce state (reagent/atom {:title "ITCH"
                              :messages []
                              :re-render-flip false}))

(defmulti handle-event (fn [data [ev-id ev-data]] ev-id))

(defmethod handle-event :default
  [data [_ msg]]
  (swap! data update-in [:messages] #(conj % msg)))

(defn app [data]
  (:re-render-flip @data)
  [views/main data])

(rf/reg-event-fx
 :itch/initialize
 (fn[{:keys [db]} [_ _]] #_[coeffects event]
   {:db (assoc db
               :initial? true
               :run-state :ready)}))

;;; See ~/Documents/git/sinet/resources-index/dev/index.html for "app"
(defn ^:export main []
  (rf/dispatch-sync [:itch/initialize]) 
  (enable-re-frisk!)            
  (when-let [root (.getElementById js/document "app")]
    (reagent/render-component [app state] root)))
