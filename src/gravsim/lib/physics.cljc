(ns gravsim.lib.physics
  (:require [gravsim.lib.trafo :as t]
            [gravsim.lib.quad :as quad]))


(def G "AU3/Msol'd2" 2.976235E-4)
(def HALO {:mass 100000 :pos [400 400]})

(defn gravity-acc [body pos dampen]
  (let [inv-dist #(/ 1 (+ dampen (Math/pow (t/v-dist %1 %2) 3)))]
    (t/scalar (* G (:mass body) (inv-dist (:pos body) pos))
              (t/sub (:pos body) pos))))

(defn gravacc-at-pos "pos [AU]" [pos n-bodies]
  (let [halo-acc (gravity-acc HALO pos 100000)
        gravity-reducer (fn [acc next-body] (t/add acc (gravity-acc next-body pos 1e-05)))]
    (reduce gravity-reducer halo-acc n-bodies)))

(defn move-in-potential [body dt potential]
  (let [acc (potential (:pos body))
        intervel (t/interpolate (* 0.5 dt) acc (:vel body))
        pos (t/interpolate dt intervel (:pos body))
        interacc (potential pos)
        vel (t/interpolate dt interacc intervel)]
    (-> body
        (assoc :vel vel)
        (assoc :pos pos))))

(defn update-body [body dt quadtree]
  (let [gravitating-bodies (quad/get-clustered (:pos body) (:id body) quadtree)
        potential (fn [pos] (gravacc-at-pos pos gravitating-bodies))]
    (move-in-potential body dt potential)))

(defn update-physics [dt bodies quadtree]
  (map #(update-body % dt quadtree) bodies))

