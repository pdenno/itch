(ns owendenno.itch.client.block
  "BlockShape handles drawing and resizing of a block."
  {:author "Owen Denno"}
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as qm]
            #?(:cljs [cljs.pprint :refer (pprint)])
            #?(:clj  [clojure.pprint :refer (pprint)])))

;;; POD: We might not use all of these. 
(def ^:private notch-depth 5)
(def ^:private chamfer "length or height of corner chamfer" 3)
(def ^:private notch-l1 13)
(def ^:private notch-l2 (+ notch-l1 notch-depth))
(def ^:private notch-r1 (+ notch-l2 8))
(def ^:private notch-r2 (+ notch-r1 notch-depth))

(def ^:private BottomBarH 16) ; height of the bottom bar of a C or E block
(def ^:private DividerH 18)   ; height of the divider bar in an E block

;;; https://www.sessions.edu/color-calculator/
(defn shape-color
  "Return a [r g b] vector for the argument color (keyword)"
  [category]
  (case category
    :motion      [0x58 0x7f 0xd4]
    :looks       [0x9b 0x30 0xcc]
    :sound       [0xba 0x4b 0xc2]
    :pen         [0x47 0xa8 0x43]
    :data        [0xeb 0x7b 0x26]
    :events      [0xb8 0x60 0x1e]
    :control     [0xd1 0x9d 0x22]
    :sensing     [0x1d 0x97 0xfa]
    :operators   [0x66 0xb8 0x3d]
    :more-blocks [0x64 0x0d 0xa3]))

;;; POD Example feature
(def foo {:cmd :say-for
          :info {:tab :looks :block :say-for :shape :cmd}
          :content ["say"
                    {:type :text   :default "Hello!"}
                    "for"
                    {:type :number :default 2}
                    "sec"]})
(declare cmd-pts block-len draw-content!)
(def tab-right "separate content element of block" 8)
(def text-rect-extend "add to right of text rectangles" 10)
(def text-y-offset "distance downward from block (xs, ys)." 15)

(defn draw-block!
  "Draw a shape and return its features object."
  [x y features]
  (let [len (block-len (:content features))
        fill-color (shape-color (-> features :info :tab))]
    (apply q/stroke fill-color) 
    (apply q/fill   fill-color)
    (q/begin-shape)
    (doall (map (fn [[x y]] (q/vertex x y)) (cmd-pts x y len)))  ; POD (draw-shape! (-> features :info :shape))
    (q/end-shape)
    (reduce (fn [xpos widget]
              (+ xpos (draw-content! widget xpos y)))
            (+ x tab-right)
            (:content features))
    (-> features
        (assoc :x x)
        (assoc :y y))))

;;; POD ToDo: Write a :number widget
(defn draw-content!
  "Draw widget/text onto the shape at argument position and return delta on x."
  [widget x y]
  (cond (string? widget)
        (do (q/fill 255)
            (q/text widget x (+ y text-y-offset))
            (+ tab-right (q/text-width widget)))
        (= :text (:type widget))
        (let [text (or (:default widget) " ")
              w    (q/text-width text)]
          (q/with-fill [255] (q/rect x (+ y 3) (+ w text-rect-extend) text-y-offset))
          (q/with-fill [0  ] (q/text text (+ x 5) (+ y text-y-offset)))
          (+ w tab-right text-rect-extend))))

(defn block-len
  "Return the required length to display the features (strings and  widget maps)."
  [features]
  (-
   (reduce (fn [sum feat]
             (if (string? feat)
               (+ sum tab-right (q/text-width feat))
               (+ sum tab-right
                  (case (:type feat)
                    :text   (+ (q/text-width (str (:default feat))) text-rect-extend)
                    :number (+ (q/text-width (str (:default feat))) text-rect-extend)))))
           0
           features)
   (* 4 tab-right))) ; POD 4: no idea!

(def cmd-thick "Thickness of the cmd-shape" 20)
;;;         nl1    nl2
;;;       2 _____3      6 ______________________7
;;;        /     \4____5/                       \ 8        ___
;;;    1  |                                      |          ^
;;;       |                                      |         cmd-thick
;;;   16  |                                      | 9       _v_
;;;        \15___ 14     _11____________________/
;;;              \______/                        10
;;;             13        12
(defn cmd-pts 
  "Return a vector of the 16 points on a command block.
   p1 = (xs,ys) is lowest point on upper RHS chamfer."
  [xs ys len]
  (let [x-vals (reduce (fn [pt-xs more] (conj pt-xs (+ (last pt-xs) more)))
                       [xs]
                       [chamfer notch-l1 chamfer notch-l2 chamfer len chamfer])
        x-vals (vec (into x-vals (reverse x-vals)))
        y-vals1 [ys (- ys chamfer) (- ys chamfer) (+ ys (- chamfer) notch-depth) (+ ys (- chamfer) notch-depth)
                 (- ys chamfer)  (- ys chamfer) ys]
        y-vals2 [ys (+ ys chamfer) (+ ys chamfer) (+ ys chamfer notch-depth) (+ ys chamfer notch-depth)
                 (+ ys chamfer)  (+ ys chamfer) ys]
        y-vals2 (mapv #(+ % cmd-thick) y-vals2)
        y-vals (into y-vals1 y-vals2)]
    (mapv #(vector %1 %2) (conj x-vals xs) (conj y-vals ys))))

(defn draw-script-blocks
  "Mostly just for testing."
  []
  (draw-block! 10 10 {:info {:tab :looks :block :say :shape :cmd}
                      :content ["say"
                                {:type :text :default "Hello!"}
                                "for"
                                {:type :text :default "1"}
                                "sec"]})
  (draw-block! 10 40 {:info {:tab :looks :block :say :shape :cmd}
                      :content ["say" {:type :text :default "Hello!"}]})
  (draw-block! 10 70 {:info {:tab :motion :block :say :shape :cmd}
                      :content ["say" {:type :text :default "Hello, Owen!"}]})
  (draw-block! 10 100 {:info {:tab :pen :block :say :shape :cmd}
                      :content ["say" {:type :text :default "Hello, Owen!"}]})
  (draw-block! 10 130 {:info {:tab :data :block :say :shape :cmd}
                       :content ["say something really long"
                                 {:type :text :default "Hello, Owen! What's more to say?"}]})
  (draw-block! 10 160 {:info {:tab :data :block :say :shape :cmd}
                       :content ["say something"
                                 {:type :text :default "Hello, Owen!"}
                                 "then wait for"
                                 {:type :text :default "2"}
                                 "sec, then wait for" 
                                 {:type :text :default "3"}
                                 "sec"]}))

               





