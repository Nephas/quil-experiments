(ns gravsim.core
  (:gen-class)
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [gravsim.rand :as r]
            [gravsim.quad :as quad]
            [gravsim.physics :as p]))

(def SCREEN [1200 900])
(def SCREENRECT [0 0 (first SCREEN) (last SCREEN)])

(def bodies (for [_ (range 1000)]
              {:pos  [(* (first SCREEN) (r/uniform 0.2 0.8)) (* (second SCREEN) (r/uniform 0.2 0.8))]
               :vel  [(r/uniform -0.1 0.1) (r/uniform -0.1 0.1)]
               :mass (r/rand-n 32 128)
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
        bodies (filter on-screen? (doall (p/update-physics (:bodies state) (:quadtree state))))
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

(defn draw-bodies [bodies]
  (q/fill 255 0 255)
  (q/no-stroke)
  (doseq [body bodies]
    (draw-circle (:pos body) (Math/sqrt (* 0.1 (:mass body))))))

(defn draw-state [state]
  (apply q/background [160 20 60])
  (draw-quadtree (:quadtree state))
  (draw-bodies (:bodies state))
  (q/text-num (q/current-frame-rate) 40 40))

(defn -main [& args]
  (q/defsketch -main
               :title "You spin my circle right round"
               :size SCREEN
               :setup setup

               :update update-state
               :draw draw-state
               :renderer :java2d

               :features [:present]
               :middleware [m/fun-mode]))