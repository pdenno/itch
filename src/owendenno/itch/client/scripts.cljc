(ns owendenno.itch.client.scripts
  "The scripts tab"
  {:author "Owen Denno"}
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as qm]
            #?(:cljs [cljs.pprint :refer (pprint)])
            #?(:clj  [clojure.pprint :refer (pprint pp)])
            [owendenno.itch.client.utils :as ut :refer (ppprint ppp)]
            [owendenno.itch.client.block :as bl]))

(def +user-state+ "Everything about the user's session." (atom {}))
(def tabs-window
  "Some parameters controlling appearance and behavior of the tabs window."
  {:window-size {:length 600 :height 400}
   :mouse-detect-range 30.0
   :background 200}) ; light grey

(def scripts-tab-blocks
  "basic information for making script tab blocks; 
   needs to be completed inside defsketch."
   ;; A basic one
   [{:x 10 :y 10 :idx 0
     :info {:tab :looks :block :say :shape :cmd}
     :content ["say" {:type :text :default "Hello!"}]}
    ;; A basic one plus "for 1 sec"
    {:x 10 :y 40 :idx 1
     :info {:tab :looks :block :say :shape :cmd}
     :content ["say"
               {:type :text :default "Hello!"}
               "for"
               {:type :text :default "1"}
               "sec"]}
    ;; more text 
    {:x 10 :y 70 :idx 2
     :info {:tab :motion :block :say :shape :cmd}
     :content ["say" {:type :text :default "Hello, Peter"}]}
    ;; same except color
    {:x 10 :y 100 :idx 3
     :info {:tab :pen :block :say :shape :cmd}
     :content ["say" {:type :text :default "Hello, Owen!"}]}
    ;; really long text
    {:x 10 :y 130 :idx 4
     :info {:tab :data :block :say :shape :cmd}
     :content ["say something really long"
               {:type :text :default "Hello, Owen! What's more to say?"}]}
    ;; text widget text widget...
    {:x 10 :y 160 :idx 5
     :info {:tab :data :block :say :shape :cmd}
     :content ["say something"
               {:type :text :default "Hello, Owen!"}
               "then wait for"
               {:type :text :default "2"}
               "sec, then wait for" 
               {:type :text :default "3"}
               "sec"]}])
#?(:cljs
   (defn ppp []
     (binding [cljs.pprint/*print-right-margin* 140]
       (pprint *1))))

#?(:cljs
   (defn ppprint [arg]
     (binding [cljs.pprint/*print-right-margin* 140]
       (pprint arg))))

(def ^:private diag "for debugging" (atom nil))
(declare nearest-elem)
(defn handle-move!
  "Mouse pressed: Update coordinates to move an element or its label."
  []
  (when-not (:image-moving @+user-state+)
    (let [mx (q/mouse-x)
          my (q/mouse-y)]
      (when-let [elem (nearest-elem (:scripts @+user-state+) mx my)]
        (swap!
         +user-state+
         #(let [idx (:idx elem)]
            (as-> % ?state
              (assoc ?state :image-moving (q/get-pixel mx my 100 100))
              ;;(assoc-in [:scripts idx :x] mx)
              ;;(assoc-in [:scripts idx :y] my)
              #_(assoc-in [:scripts idx :points] (bl/cmd-pts mx my (:len elem))))))))))

(def foo (fn [x y] (+ x y)))
(def bar #(+ %1 %2))

(defn nearest-elem
  "Return a 'block map' indicating what was closest to the mouse.
   Returns nil if nothing is close."
  [blocks mx my]
  (let [[blk min-d]
        (reduce
         (fn [[best min-d] blk]
           (let [dist (Math/round (ut/distance mx my (:x blk) (:y blk)))]
             (if (< dist min-d)
               [blk dist]
               [best min-d])))
         [nil 99999]
         blocks)]
    (when (< min-d (:mouse-detect-range tabs-window))
      blk)))

(defn setup-scripts []
  (q/frame-rate 20) ; fps
  (q/text-font (q/create-font "Verdana-Bold" 12 true))
  (q/background (:background tabs-window))   
  (q/stroke-weight 1)
  (swap! +user-state+
         #(assoc % :scripts (mapv bl/make-block scripts-tab-blocks))))

(def redraw-at "value of skip-cnt where everything is redrawn and skip-cnt reset" 101)
(def skip-cnt  "counter for redrawing" (atom redraw-at))
(defn draw-scripts
  "defsketch's :draw function; called FPS times every second."
  []
  (if (q/mouse-pressed?)
    (handle-move!) ; POD should be 'detect-pointed-at' or some such thing. 
    true #_(swap! +user-state+ #(assoc % :image-moving nil)))
  (let [move-me (:image-moving @+user-state+)]
    (when (or move-me (>= @skip-cnt redraw-at))
      ;; Redraw everything. 
      (reset! skip-cnt 0)
      (q/background (:background tabs-window)) ; clears things, I think. 
      (when move-me (q/set-image (q/mouse-x) (q/mouse-y) move-me))
      (doseq [blk (:scripts @+user-state+)] (bl/draw-block! blk)))
    (swap! skip-cnt inc)))

#?(:clj
 (defn show-it []
  (q/defsketch scripts-sketch
    :host "scripts-tab"
    :title "Scripts"
    :features [:keep-on-top] 
    :settings #(fn [] (q/smooth 2) ; Smooth=2 is typical. Can't use pixel-density with js.
                 #?(:clj (q/pixel-density (q/display-density))))
    :setup setup-scripts
    :draw  draw-scripts
    :size [(-> tabs-window :window-size :length)
           (-> tabs-window :window-size :height)])))

