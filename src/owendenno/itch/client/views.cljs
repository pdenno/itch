(ns owendenno.itch.client.views
  (:require [clojure.string :as str]
            [clojure.pprint :refer (cl-format pprint)]
            [quil.core :as quil :include-macros true]
            [quil.middleware :as qm]
            [owendenno.itch.client.scripts :as scripts :refer (setup-scripts draw-scripts +scripts+)]
            [reagent.core :as reagent]
            [re-frame.core :as rf]))

(rf/reg-sub :initial?  (fn [db _] (:initial? db)))
(rf/reg-sub :scripts   (fn [db _] (:scripts db)))
(rf/reg-sub :run-state (fn [db _] (:run-state db)))

;;; https://www.w3schools.com/bootstrap/bootstrap_theme_company.asp
(defn nav []
  [:nav {:class "navbar navbar-default navbar-fixed-top"
         :style {:margin-bottom "0" :background-color "#330066" :z-index "9999"
                 :border "0" :font-size "14px" :line-height "1.42857143" 
                 :letter-spacing "4px" :border-radius "0"}}
   [:div {:class "container"}
    [:div {:class "navbar-header"}
     [:button {:type "button" :class "navbar-toggle" :data-toggle "collapse" :data-target "#myNavbar" :style {:color "#ffffff"}} ; ditto
      [:span {:class "icon-bar"}]
      [:span {:class "icon-bar"}]
      [:span {:class "icon-bar"}]]
     [:a {:class "navbar-brand" :href "#myPage" :style {:color "#ffffff"}} "ITCH" ]] 
    [:div {:class "collapse navbar-collapse" :id "myNavbar"} 
     [:ul {:class "nav navbar-nav navbar-right"} 
      [:li [:a {:href "#File"}"File"]]
      [:li [:a {:href "#Edit"}"Edit"]]
      [:li [:a {:href "#Tips"}"Tips"]]
      [:li [:a {:href "#About"}"About"]]]]]])

(declare quil-scripts-tab)

(defn quil-scripts []
  (let [_ @(rf/subscribe [:scripts])
        _ @(rf/subscribe [:initial?])]
          (quil-scripts-tab)
    [:canvas {:id "scripts-tab"}]))

(defn scripts-area []
  [:div#draw {:class "col-md-8"}
   [quil-scripts]])

(defn buttons []
  (let [run-state @(rf/subscribe [:run-state])]
    [:div {:class "container"}
     [:div {:class "row"}
      [:div {:class "btn-group btn-group-sm"}
       [:button {:class "btn btn-primary" :style {:background-color "#CC0066"}
                 :disabled (= run-state :run)
                 :on-click #(rf/dispatch [:itch/run-state :run])} "Run"]
       [:button {:class "btn btn-primary" :style {:background-color "#CC0066"}
                 :disabled (not (= run-state :run))
                 :on-click #(rf/dispatch [:itch/run-state :pause])} "Stop"]]]]))


;;; To render the DOM representation of some part of the app state, view functions must query
;;; for that part of app-db, and that means using subscribe.
(defn report []
  (let [rmap @(rf/subscribe [:report])]
    [:div {:class "container"}
     [:div {:class "row"}
      [:p [:strong (cl-format  nil "Report")]]]
     (when (and (contains? rmap :generation) (contains? rmap :pop-size))
       (doall
        (for [key (keys rmap)]
          [:div {:class "row" :key key}
           [:div 
            (str (->> (str/split (str/replace (subs (str key) 1) #"\-+" " ") " ")
                      (map str/capitalize)
                      (interpose " ")
                      (str/join))
                 ": "
                 (get rmap key))]])))]))

(defn buttons-area []
  [:div#buttons-report {:class "col-md-4"}
   [:div {:class "container-fluid"}
    [:div {:class "row"} [buttons]]]])

;;; Util for logging output to on-screen console
(defn console-area []
  [:div {:class "col-md-12"}
   [:strong "Console"]
   [:textarea {:value "foo" :readOnly true
               ;;:on-change (fn [e] (aset e "scrollTop" (.-scrollHeight e))) ; POD does nothing
               :on-change #(.-scrollHeight %) ; POD does nothing
               :style {:width "100%" :height "200px" :font-size "small"}}]])

(defn quil-scripts-tab []
  (quil/defsketch scripts-tab 
    :host "scripts-tab"
    :title "Scripts"
    :settings #(fn [] (quil/smooth 2)) ; Smooth=2 is typical. Can't use pixel-density with js.
    :setup scripts/setup-scripts
    :draw scripts/draw-scripts
    ;; POD I need a solution for getting it here! 
    :size [(-> scripts/graph-window-params :window-size :length)
           (-> scripts/graph-window-params :window-size :height)]))

(defn main [data]
  (let [_ @(rf/subscribe [:initial?])]
    [:div {:id "myPage" :data-spy "scroll" :data-target ".navbar" :data-offset "60"} ; was :body. Could probably go back!
     [nav]
     [:div {:class "jumbotron text-center" :style {:background-color "#330066" :color "#ffffff"}} ; 
      [:h1 "Itch"]
      [:p "A Scratch-like environment for functional programming"]]
     [:div {:class "container-fluid"}
      [:div {:class "row"}
       [buttons-area]
       [scripts-area]]
      [:div {:class "row"}
       [console-area]]]]))

