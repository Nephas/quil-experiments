(ns gravsim.lib.quad
  (:require [gravsim.lib.trafo :as t]))

(def X 0)
(def Y 1)

(def WIDTH 2)
(def HEIGHT 3)

(def THRESHOLD 1.5)

(defn half-width [rect] (* 0.5 (get rect WIDTH)))
(defn half-height [rect] (* 0.5 (get rect HEIGHT)))

(defn center [rect]
  [(+ (get rect X) (half-width rect))
   (+ (get rect Y) (half-height rect))])

(defn area [rect] (* (get rect WIDTH) (get rect HEIGHT)))

(defn slice [rect [center-x center-y] direction]
  (let [height (half-width rect)
        width (half-height rect)]
    (case direction 0 [(get rect X) (get rect Y) height width]
                    1 [center-x (get rect Y) height width]
                    2 [(get rect X) center-y height width]
                    3 [center-x center-y height width])))

(defn group-by-quad [[center-x center-y] bodies]
  (loop [remaining bodies
         nw [] ne [] sw [] se []]
    (if (empty? remaining) [nw ne sw se]
                           (let [body (first remaining)
                                 west? (< (get (:pos body) X) center-x)
                                 north? (< (get (:pos body) Y) center-y)]
                             (cond (and west? north?) (recur (rest remaining) (conj nw body) ne sw se)
                                   (and (not west?) (not north?)) (recur (rest remaining) nw ne sw (conj se body))
                                   west? (recur (rest remaining) nw ne (conj sw body) se)
                                   north? (recur (rest remaining) nw (conj ne body) sw se))))))

(defn quadtree-node [rect bodies]
  (let [mass (apply + (map #(:mass %) bodies))
        center (center rect)
        num (count bodies)
        density (/ mass (area rect))
        node {:rect    rect
              :pos     center
              :mass    mass
              :density density
              :leaf    true}]

    (cond (zero? num) nil
          (= num 1) (-> node
                        (assoc :bodies (first bodies))
                        (assoc :leaf true))
          true (let [grouped (group-by-quad center bodies)
                     children (mapv (fn [dir group] (quadtree-node (slice rect center dir) group))
                                    [0 1 2 3] grouped)]
                 (-> node
                     (assoc :children (filterv some? children))
                     (assoc :leaf false))))))

(defn get-clustered [pos id node]
  (let [dist-par (/ (get-in node [:rect WIDTH]) (t/v-dist pos (:pos node)))
        far-node? (< dist-par THRESHOLD)
        self? (fn [body] (= id (body :id)))]
    (cond (:leaf node) (let [body (:bodies node)]
                         (if (self? body) [] body))
          far-node? {:pos  (:pos node)
                     :mass (:mass node)}
          true (map #(get-clustered pos id %) (:children node)))))