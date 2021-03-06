(ns conway.core
  (:gen-class)
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [conway.lib.grid :as g]
            [conway.lib.rules :as r]))

(def get-X #(get % 0))
(def get-Y #(get % 1))

(def SCREEN [800 800])

(def CELLSIZE 10)
(def MAPSIZE [(int (/ (get-X SCREEN) CELLSIZE)) (int (/ (get-Y SCREEN) CELLSIZE))])

(defn setup []
  (q/frame-rate 30)
  (q/color-mode :hsb)
  (apply q/background [20 20 70])
  ; return initial-state
  {:grid (g/generate-grid (get-X MAPSIZE) (get-Y MAPSIZE))
   :pause false})

(defn update-state [state]
  (if (and (not (:pause state)) (zero? (mod (q/frame-count) 10)))
    (update state :grid r/evolve-grid)
    state))

(defn draw-cell [[x y] alive]
  (when alive
    (q/rect (* CELLSIZE x) (* CELLSIZE y) CELLSIZE CELLSIZE)))

(defn draw-state [state]
  (apply q/background [160 100 0])
  (doall (map (fn [kv] (draw-cell (first kv) (last kv)))
              (:grid state)))
  (q/text (str "FPS: " (int (q/current-frame-rate))) 40 40)
  (when (:pause state)
    (q/text (str "Paused") 40 80)))

(defn handle-click [state event]
  (println event)
  (let [pos (g/pix-to-grid event CELLSIZE)]
    (cond
      (= :left (:button event)) (update-in state [:grid pos] not)
      (= :center (:button event)) (update state :pause not)
      (= :right (:button event)) (let [target-val (not (get-in state [:grid pos]))]
                                   (loop [targets (g/get-neighborhood pos)
                                          next state] (if (empty? targets) next
                                                                           (recur (rest targets)
                                                                                  (assoc-in next [:grid (first targets)] target-val)))))
      true state)))

(defn -main [& args]
  (q/defsketch -main
               :title "conway"
               :size SCREEN
               :renderer :java2d

               :setup setup
               :update update-state
               :draw draw-state
               :mouse-clicked handle-click

               ;:features [:present]
               :bgcolor "#111122"

               :middleware [m/fun-mode]))
