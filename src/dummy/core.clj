(ns dummy.core
  (:gen-class)
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(def SCREEN [800 800])
(def SCREENRECT [0 0 (first SCREEN) (last SCREEN)])

(defn setup []
  (q/frame-rate 30)
  (q/color-mode :hsb)
  (apply q/background [20 20 70])
  ; return initial-state
  {:flag false})

(defn update-state [state] state)

(defn draw-state [state]
  (apply q/background [160 100 0])
  (let [phase-x (mod (q/frame-count) 50)
        phase-y (mod (+ (q/frame-count) 25) 50)]
    (q/ellipse (* 0.5 (get SCREEN 0)) (* 0.5 (get SCREEN 1)) phase-x phase-y))
  (q/text (str "FPS: " (int (q/current-frame-rate))) 40 40)
  (q/text (str "Flag: " (:flag state)) 40 80))

(defn handle-click [state event]
  (cond
    (= :left (:button event)) (-> state (update :flag not))
    (= :right (:button event)) state
    true state))

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
