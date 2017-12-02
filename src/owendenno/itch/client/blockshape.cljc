(ns owendenno.itch.client.blockshape
  "BlockShape handles drawing and resizing of a block."
  {:author "Owen Denno"}
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as qm]
            #?(:cljs [cljs.pprint :refer (pprint)])
            #?(:clj  [clojure.pprint :refer (pprint)])))

;;; POD: We might not use all of these. 
(def ^:private notch-depth 5)
(def ^:private chamfer "length or height of corner chamfer" 3)
(def ^:private InnerCornerInset 2)
(def ^:private notch-l1 13)
(def ^:private notch-l2 (+ notch-l1 notch-depth))
(def ^:private notch-r1 (+ notch-l2 8))
(def ^:private notch-r2 (+ notch-r1 notch-depth))

(def ^:private BottomBarH 16) ; height of the bottom bar of a C or E block
(def ^:private DividerH 18)   ; height of the divider bar in an E block

(declare cmd-pts)
(defn cmd-shape [xs ys]
  "Draw a command shape."
  (q/stroke 70 80 255) ; lighter blue) ; black
  (q/fill 70 80 255) ; lighter blue
  (q/begin-shape)
  (doall (map (fn [[x y]] (q/vertex x y)) (cmd-pts xs ys 60))) ; 3rd arg is adjustable length.
  (q/end-shape))

(def cmd-thick "Thickness of the cmd-shape" 18)
;;;         nl1    nl2
;;;       2 _____3      6 ______________________7
;;;        /     \4____5/                       \ 8        ___
;;;    1  |                                      |          ^
;;;       |                                      |         cmd-thick
;;;   16  |                                      | 9       _v_
;;;        \15___ 14     _11____________________/
;;;              \______/                        10
;;;             13        12
(defn cmd-pts [xs ys len]
  "Return a vector of the 16 points on a command block.
   p1 = (xs,ys) is lowest point on upper RHS chamfer."
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


