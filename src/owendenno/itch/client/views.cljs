(ns owendenno.itch.client.views
  (:require [clojure.string :as str]
            [clojure.pprint :refer (cl-format pprint)]
            [quil.core :as quil :include-macros true]
            [quil.middleware :as qm]
            [owendenno.itch.client.draw :as draw :refer (setup-pn draw-pn pn-wheel-fn +display-pn+)]
            [reagent.core :as reagent]
            [re-frame.core :as rf]))

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
      [:li [:a {:href "#gp"}"GP"]]
      [:li [:a {:href "#params"}"Parameters"]]
      [:li [:a {:href "#patterns"}"Message Patterns"]]]]]])

(declare draw-it)

(defn quil-pn []
  (let [pn @(rf/subscribe [:pn])]
    (when (contains? pn :places)
      (reset! draw/+display-pn+ (draw/pn-geom pn))
      (draw-it))
    [:canvas {:id "best-pn"}]))

(defn drawing-area []
  [:div#pn {:class "col-md-8"}
   [quil-pn]])

(defn buttons []
  (let [evolve-state @(rf/subscribe [:evolve-state])
        pn-id        @(rf/subscribe [:requested-pn])
        report       @(rf/subscribe [:report])]
    [:div {:class "container"}
     [:div {:class "row"} [:strong "GP Control"]]
     [:div {:class "row"} "Viewing PN (order): " pn-id]
     [:div {:class "row"}
      [:div {:class "btn-group btn-group-sm"}
       [:button {:class "btn btn-primary" :style {:background-color "#CC0066"}
                 :disabled (not report)
                 :on-click #(rf/dispatch [:sinet/requested-pn 1])} "Pop+"]
       [:button {:class "btn btn-primary" :style {:background-color "#CC0066"}
                 :disabled (or (= pn-id :none) (= pn-id 0) )
                 :on-click #(rf/dispatch [:sinet/requested-pn -1])} "Pop-"]]]
     [:div {:class "row"}
      [:div {:class "btn-group btn-group-sm"}
       [:button {:class "btn btn-primary" :style {:background-color "#CC0066"}
                 :disabled (= evolve-state :run)
                 :on-click #(rf/dispatch [:sinet/evolve-state :run])} "Run"]
       [:button {:class "btn btn-primary" :style {:background-color "#CC0066"}
                 :disabled (not (= evolve-state :run))
                 :on-click #(rf/dispatch [:sinet/evolve-state :pause])} "Pause"]
       [:button {:class "btn btn-primary" :style {:background-color "#CC0066"}
                 :disabled (not (= evolve-state :pause))
                 :on-click #(rf/dispatch [:sinet/evolve-state :continue])} "Continue"]
       [:button {:class "btn btn-primary" :style {:background-color "#CC0066"}
                 :on-click #(rf/dispatch [:sinet/evolve-state :abort])} "Abort"]]]]))


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

(defn buttons-report []
  [:div#buttons-report {:class "col-md-4"}
   [:div {:class "container-fluid"}
    [:div {:class "row"} [buttons]]
    [:div {:class "row"} [report]]]])

;;; Util for logging output to on-screen console
(defn console-area []
  [:div {:class "col-md-12"}
   [:strong "Console"]
   [:textarea {:value "foo" :readOnly true
               ;;:on-change (fn [e] (aset e "scrollTop" (.-scrollHeight e))) ; POD does nothing
               :on-change #(.-scrollHeight %) ; POD does nothing
               :style {:width "100%" :height "200px" :font-size "small"}}]])

(defn draw-it []
  (quil/defsketch best-pn 
    :host "best-pn"
    :title "Best Individual"
    :settings #(fn [] (quil/smooth 2)) ; Smooth=2 is typical. Can't use pixel-density with js.
    :setup draw/setup-pn
    :draw draw/draw-pn
    :mouse-wheel draw/pn-wheel-fn
    ;; POD I need a solution for getting it here! 
    :size [(-> draw/graph-window-params :window-size :length)
           (-> draw/graph-window-params :window-size :height)]))

(defn main [data]
  (let [_ @(rf/subscribe [:initial?])]
    [:div {:id "myPage" :data-spy "scroll" :data-target ".navbar" :data-offset "60"} ; was :body. Could probably go back!
     [nav]
     [:div {:class "jumbotron text-center" :style {:background-color "#330066" :color "#ffffff"}} ; 
      [:h1 "Itch"]
      [:p "System Identification for Smart Manufacturing"]]
     [:div {:class "container-fluid"}
      [:div {:class "row"}
       [drawing-area]
       [buttons-report]]
      [:div {:class "row"}
       [console-area]]]]))

