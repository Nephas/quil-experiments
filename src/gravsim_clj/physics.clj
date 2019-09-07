(ns gravsim_clj.physics
  (:require [gravsim_clj.trafo :as t]
            [gravsim_clj.quad :as quad]))


(def G "AU3/Msol'd2" 2.976235E-4)

(defn gravacc-at-pos "pos [AU]" [pos n-bodies]
  (let [inv-dist #(/ 1 (+ 1e-3 (Math/pow (t/v-dist %1 %2) 3)))
        nth-acc (fn [body] (t/scalar (* G (:mass body) (inv-dist pos (:pos body)))
                                     (t/sub (:pos body) pos)))]
    (reduce t/add (map nth-acc n-bodies))))

(defn move [body dt]
  (update body :pos t/add (t/scalar dt (:vel body))))

(defn accelerate [body dt quadtree]
  (let [not-self? (fn [other] (not= (:id body) (:id other)))
        n-bodies (filter not-self? (quad/get-clustered (:pos body) quadtree))
        acc (gravacc-at-pos (:pos body) n-bodies)]
    (if (empty? n-bodies) body
                          (update body :vel t/add (t/scalar dt acc)))))

(defn update-physics [bodies quadtree]
  (let [dt 0.5] (map #(-> %
                          (accelerate dt quadtree)
                          (move dt))
                     bodies)))

