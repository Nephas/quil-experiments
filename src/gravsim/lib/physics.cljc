(ns gravsim.lib.physics
  (:require [gravsim.lib.trafo :as t]
            [gravsim.lib.quad :as quad]))


(def G "AU3/Msol'd2" 2.976235E-4)
(def HALO {:mass 100000 :pos [600 450]})

(defn gravity-acc [body pos dampen]
  (let [inv-dist #(/ 1 (+ dampen (Math/pow (t/v-dist %1 %2) 3)))]
    (t/scalar (* G (:mass body) (inv-dist (:pos body) pos))
              (t/sub (:pos body) pos))))

(defn gravacc-at-pos "pos [AU]" [pos n-bodies]
  (let [n-bodies-acc (map #(gravity-acc % pos 1e-05) n-bodies)
        halo-acc (gravity-acc HALO pos 100000)]
    (reduce t/add (conj n-bodies-acc halo-acc))))

(defn move-in-potential [body dt n-bodies]
  (let [acc (gravacc-at-pos (:pos body) n-bodies)
        intervel (t/add (t/scalar (* 0.5 dt) acc) (:vel body))
        pos (t/add (t/scalar dt intervel) (:pos body))
        interacc (gravacc-at-pos pos n-bodies)
        vel (t/add (t/scalar dt interacc) intervel)]
    (-> body
        (assoc :acc acc)
        (assoc :vel vel)
        (assoc :pos pos))))

(defn get-gravitating-bodies [body quadtree]
  (let [not-self? (fn [other] (not= (:id body) (:id other)))
        n-bodies (quad/get-clustered (:pos body) quadtree)]
    (filterv not-self? (flatten n-bodies))))

(defn update-body [body dt quadtree]
  (let [n-bodies (get-gravitating-bodies body quadtree)]
    (move-in-potential body dt n-bodies)))

(defn update-physics [dt bodies quadtree]
    (pmap #(update-body % dt quadtree) bodies))

