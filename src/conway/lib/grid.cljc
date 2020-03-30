(ns conway.lib.grid
  (:require [dummy.lib.rand :as r]))


(def DIRECTIONS '([-1 -1] [-1 0] [-1 1]
                  [0 -1] [0 1]
                  [1 -1] [1 0] [1 1]))

(defn pix-to-grid [{x :x y :y} cellsize]
  [(int (/ x cellsize)) (int (/ y cellsize))])

(defn generate-grid-keys [x-max y-max]
  (apply concat
         (map (fn [x]
                (map (fn [y] [x y])
                     (range y-max)))
              (range x-max))))

(defn generate-grid [x-max y-max]
  (let [keys (generate-grid-keys x-max y-max)
        vals (map (fn [_] (r/rand-bool)) (range (count keys)))]
    (zipmap keys vals)))

(defn add [[x1 y1] [x2 y2]]
  [(+ x1 x2) (+ y1 y2)])

(defn get-neighborhood [pos]
  (map (fn [dir] (add pos dir)) DIRECTIONS))