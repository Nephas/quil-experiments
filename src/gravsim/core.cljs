(ns gravsim.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [gravsim.lib.rand :as r]
            [gravsim.lib.quad :as quad]
            [gravsim.lib.physics :as p]))

(def SCREEN [800 800])
(def SCREENRECT [0 0 (first SCREEN) (last SCREEN)])

(def bodies (for [_ (range 250)]
              {:pos  [(* (first SCREEN) (r/uniform 0.3 0.7)) (* (second SCREEN) (r/uniform 0.3 0.7))]
               :vel  [(r/uniform -0.1 0.1) (r/uniform -0.1 0.1)]
               :mass (r/rand-n 10 50)
               :id   (r/rand-n 4096)}))

(def store (atom {:bodies   bodies
                  :quadtree (quad/quadtree-node SCREENRECT bodies)}))

(defn setup []
  (q/frame-rate 30)
  (q/color-mode :hsb)
  (apply q/background [20 20 70])
  ; return initial-state
  @store)

(defn update-state [state]
  (let [on-screen? (fn [body] (let [[x y] (:pos body)] (and (< 0 x (first SCREEN)) (< 0 y (second SCREEN)))))
        bodies (filter on-screen? (doall (p/update-physics 0.5 (:bodies state) (:quadtree state))))
        quadtree (doall (quad/quadtree-node SCREENRECT bodies))]
    (reset! store (-> state
                      (assoc :bodies bodies)
                      (assoc :quadtree quadtree)))))

(defn draw-circle [[x y] radius]
  (q/ellipse x y radius radius))

(defn draw-node [node]
  (when (and (some? node) (seq (:children node)))
    (q/fill 160 100 (* 100 (:density node)))
    (apply q/rect (:rect node))
    (doseq [child (:children node)]
      (draw-node child))))

(defn draw-quadtree [node]
  (q/stroke-weight 1)
  (q/stroke 160 60 160)
  (q/no-fill)
  (draw-node node))

(defn draw-bodies [bodies trajectories?]
  (q/fill 255 0 255)
  (q/no-stroke)
  (doseq [body bodies]
    (let [radius (if trajectories? 0.5 (Math/sqrt (* 0.1 (:mass body))))]
      (draw-circle (:pos body) radius))))

(defn draw-state [state]
  (when (not (:show-trajectories state))
    (apply q/background [160 100 0]))
  (when (:show-tree state)
    (draw-quadtree (:quadtree state)))
  (draw-bodies (:bodies state) (:show-trajectories state))
  (q/text-num (q/current-frame-rate) 40 40))

(defn handle-click [state event]
  (apply q/background [160 100 0])
  (cond
    (= :left (:button event)) (-> state
                                  (update :show-tree not)
                                  (assoc :show-trajectories false))
    (= :right (:button event)) (-> state
                                   (update :show-trajectories not)
                                   (assoc :show-tree false))
    true state))

(q/defsketch -main
             :title "gravsim"
             :size SCREEN
             :host "canvas"

             :setup setup
             :update update-state
             :draw draw-state
             :mouse-clicked handle-click

             :features []
             :middleware [m/fun-mode])