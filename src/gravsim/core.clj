(ns gravsim.core
  (:gen-class)
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [gravsim.lib.rand :as r]
            [gravsim.lib.quad :as quad]
            [gravsim.lib.physics :as p]))

(def SCREEN [1200 900])
(def SCREENRECT [0 0 (first SCREEN) (last SCREEN)])

(def bodies (for [_ (range 500)]
              {:pos  [(* (first SCREEN) (r/uniform 0.4 0.6)) (* (second SCREEN) (r/uniform 0.4 0.6))]
               :vel  [(r/uniform -0.1 0.1) (r/uniform -0.1 0.1)]
               :mass (r/rand-n 10 100)
               :id   (r/rand-n 4096)}))

(defn setup []
  (q/frame-rate 30)
  (q/color-mode :hsb)
  (apply q/background [20 20 70])
  ; return initial-state
  {:bodies    bodies
   :quadtree  (quad/quadtree-node SCREENRECT bodies)
   :show-tree false})

(defn update-state [state]
  (let [on-screen? (fn [body] (let [[x y] (:pos body)] (and (< 0 x (first SCREEN)) (< 0 y (second SCREEN)))))
        bodies (filter on-screen? (doall (p/update-physics 1 (:bodies state) (:quadtree state))))
        quadtree (doall (quad/quadtree-node SCREENRECT bodies))]
    (-> state
        (assoc :bodies bodies)
        (assoc :quadtree quadtree))))

(defn draw-circle [[x y] radius]
  (q/ellipse x y radius radius))

(defn draw-node [node]
  (when (some? node)
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
  (apply q/background [160 100 0])
  (when (:show-tree state)
    (draw-quadtree (:quadtree state)))
  (draw-bodies (:bodies state))
  (q/text-num (q/current-frame-rate) 40 40))

(defn handle-click [state event]
  (if (= :left (:button event))
    (update state :show-tree not)
    state))

(defn -main [& args]
  (q/defsketch -main
               :title "gravsim"
               :size SCREEN
               :renderer :java2d

               :setup setup
               :update update-state
               :draw draw-state
               :mouse-clicked handle-click

               ;:features [:present]
               :bgcolor "#111122"

               :middleware [m/fun-mode]))
