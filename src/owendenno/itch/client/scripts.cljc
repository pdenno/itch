(ns owendenno.itch.client.scripts
  "The scripts tab"
  {:author "Owen Denno"}
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as qm]
            #?(:cljs [cljs.pprint :refer (pprint)])
            #?(:clj  [clojure.pprint :refer (pprint pp)])
            [owendenno.itch.client.block :as bl]))

(def +user-state+ (atom nil))

(def scripts-tab-blocks
  "basic information for making script tab blocks; 
   needs to be completed inside defsketch."
  (vector
   ;; A basic one
   {:x 10 :y 10
    :info {:tab :looks :block :say :shape :cmd}
    :content ["say" {:type :text :default "Hello!"}]}
   ;; A basic one plus "for 1 sec"
   {:x 10 :y 40
    :info {:tab :looks :block :say :shape :cmd}
    :content ["say"
              {:type :text :default "Hello!"}
              "for"
              {:type :text :default "1"}
              "sec"]}
   ;; more text 
   {:x 10 :y 70
    :info {:tab :motion :block :say :shape :cmd}
    :content ["say" {:type :text :default "Hello, Owen!"}]}
   ;; same except color
   {:x 10 :y 100
    :info {:tab :pen :block :say :shape :cmd}
    :content ["say" {:type :text :default "Hello, Owen!"}]}
   ;; really long text
   {:x 10 :y 130
    :info {:tab :data :block :say :shape :cmd}
    :content ["say something really long"
              {:type :text :default "Hello, Owen! What's more to say?"}]}
   ;; text widget text widget...
   {:x 10 :y 160
                   :info {:tab :data :block :say :shape :cmd}
                   :content ["say something"
                             {:type :text :default "Hello, Owen!"}
                             "then wait for"
                              {:type :text :default "2"}
                             "sec, then wait for" 
                             {:type :text :default "3"}
                             "sec"]}))
#?(:cljs
   (defn ppp []
     (binding [cljs.pprint/*print-right-margin* 140]
       (pprint *1))))

#?(:cljs
   (defn ppprint [arg]
     (binding [cljs.pprint/*print-right-margin* 140]
       (pprint arg))))

(def +lock-mouse-on+ (atom nil))
(def +hilite-elem+ (atom nil))

(defn rotate [x y theta]
  "Rotate (x,y) theta radians about origin."
  {:x (double (- (* (Math/cos theta) x) (* (Math/sin theta) y)))
   :y (double (+ (* (Math/sin theta) x) (* (Math/cos theta) y)))}) 

(declare nearest-elem angle distance hilite-elem! handle-move!)

(def grabbed-image (atom nil))
(defn get-image! []
  (when (empty? @grabbed-image)
    (reset! grabbed-image (q/get-pixel (q/mouse-x) (q/mouse-y) 100 100))))

(def +diag+ (atom nil))

(defn handle-move!
  "Mouse pressed: Update coordinates to move an element or its label."
  []
  (when-let [elem (or @+lock-mouse-on+ (nearest-elem @+user-state+ [(q/mouse-x) (q/mouse-y)]))]
    (swap!
     +user-state+
     (fn [_] nil))))

(defn hilite-elem!
  "Set +hilight-elem+ and maybe +lock-mouse-on+."
  [scripts]
  (let [nearest (or @+lock-mouse-on+ (nearest-elem scripts [(q/mouse-x) (q/mouse-y)]))]
    (when (and nearest (q/mouse-pressed?)) (reset! +lock-mouse-on+ nearest))
    (if nearest
      (reset! +hilite-elem+ nearest)
      (reset! +hilite-elem+ nil))))

(defn nearest-elem
  "Return a 'scripts map' indicating what was closest to the mouse.
   Returns nil if nothing is close."
  [scripts mxy]
  (let [[bkey min-d]
        (reduce
         (fn [[bkey min-d] [key val]]
           (let [dscript  (Math/round (distance (into mxy (vector (:x val) (:y val)))))
                 min? (min dscript min-d)]
               [key min?]))
         [:not-set 99999 true]
         scripts)]
    (when (< min-d 30.0)
      bkey)))

(defn distance
  ([x1 y1 x2 y2] (Math/sqrt (+ (Math/pow (- x1 x2) 2) (Math/pow (- y1 y2) 2))))
  ([line] (let [[x1 y1 x2 y2] line] (distance x1 y1 x2 y2))))

(defn angle [x1 y1 x2 y2]
  "Calculate angle from horizontal."
  (let [scale (distance x1 y1 x2 y2)]
    (when (> scale 0)
      (let [xr (/ (- x2 x1) scale)
            yr (/ (- y2 y1) scale)]
        (cond (and (>= xr 0) (>= yr 0)) (Math/acos xr),
              (and (>= xr 0) (<= yr 0)) (- (* 2.0 Math/PI) (Math/acos xr)),
              (and (<= xr 0) (>= yr 0)) (Math/acos xr)
              :else  (- (* 2.0 Math/PI) (Math/acos xr)))))))

(defn intersect-circle
  "http://mathworld.wolfram.com/Circle-LineIntersection.html"
  [x1 y1 x2 y2 r]
  (let [dx (- x2 x1)
        dy (- y2 y1)
        dr (Math/sqrt (+ (* dx dx) (* dy dy)))
        D (- (* x1 y2) (* x2 y1))
        sgnDy (if (< dy 0) -1.0 1.0)
        rootTerm (Math/sqrt (- (* r r dr dr) (* D D)))
        denom (* dr dr)]
    {:x1 (/ (+ (* D dy) (* sgnDy dx rootTerm)) denom)
     :y1 (/ (+ (- (* D dx)) (* (Math/abs dy) rootTerm)) denom)
     :x2 (/ (- (* D dy) (* sgnDy dx rootTerm)) denom)
     :y2 (/ (- (- (* D dx)) (* (Math/abs dy) rootTerm)) denom)}))

(def graph-window-params {:window-size {:length 600 :height 400}
                          :x-start 30 :y-start 30})

(defn draw-scripts []
  (q/background 230)
  (q/stroke-weight 1)
  #_(hilite-elem! @+user-state+)
  #_(if (q/mouse-pressed?)
    (handle-move!)
    (reset! +lock-mouse-on+ nil))
  (doseq [blk @+user-state+] (bl/draw-block! blk)))

(defn setup-scripts []
  (q/frame-rate 20)    ; FPS. 10 is good
  (q/text-font (q/create-font "Verdana-Bold" 12 true))
  (q/background 200)   ; light grey
  (reset! +user-state+
          (mapv bl/make-block scripts-tab-blocks)))

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
    :size [(-> graph-window-params :window-size :length)
           (-> graph-window-params :window-size :height)])))

