(ns gravsim.lib.draw
  (:require [quil.core :as q]
            [gravsim.lib.quad :as quad]))

(defn draw-circle [[x y] radius]
  (q/ellipse x y radius radius))

(defn draw-node [node]
  (when (some? node)
    (q/fill 160 100 (* 100 (quad/density node)))
    (apply q/rect (:rect node))
    (when (not (:leaf node))
      (doseq [child (:children node)]
        (draw-node child)))))

(defn draw-quadtree [node]
  (q/stroke-weight 1)
  (q/stroke 160 60 160)
  (q/no-fill)
  (draw-node node))

(defn draw-bodies [bodies trajectories?]
  (q/fill 255 0 255)
  (q/no-stroke)
  (doseq [body bodies]
    (let [radius (if trajectories? 0.5 (Math/sqrt (* 0.25 (:mass body))))]
      (draw-circle (:pos body) radius))))