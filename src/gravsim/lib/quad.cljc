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

(defn slice [rect direction]
  (let [[center-x center-y] (center rect)
        height (half-width rect)
        width (half-height rect)]
    (case direction 0 [(get rect X) (get rect Y) height width]
                    1 [center-x (get rect Y) height width]
                    2 [(get rect X) center-y height width]
                    3 [center-x center-y height width])))

(defn get-center-of-mass [body1 body2]
  (let [{pos1  :pos
         mass1 :mass} body1
        {pos2  :pos
         mass2 :mass} body2
        mass (+ mass1 mass2)
        x (/ (+ (* (get pos1 X) mass1) (* (get pos2 X) mass2)) mass)
        y (/ (+ (* (get pos1 Y) mass1) (* (get pos2 Y) mass2)) mass)]
    {:pos  [x y]
     :mass mass}))

(defn group-by-quadrant [[center-x center-y] bodies]
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

(defrecord Node [rect children mass pos leaf])

(defn density [node]
  (/ (:mass node) (area (:rect node))))

(defn quadtree-node [rect bodies]
  (let [num (count bodies)]
    (cond (zero? num) nil
          (= num 1) (let [com (first bodies)]
                      (->Node rect com (:mass com) (:pos com) true))
          true (let [com (reduce get-center-of-mass bodies)
                     groups (group-by-quadrant (center rect) bodies)
                     children (filter some? (map (fn [dir group] (quadtree-node (slice rect dir) group))
                                                   [0 1 2 3] groups))]
                 (->Node rect children (:mass com) (:pos com) false)))))

(defn get-clustered [pos id node]
  (let [far-node? (fn [node] (< (/ (get-in node [:rect WIDTH]) (t/v-dist pos (:pos node))) THRESHOLD))
        self? (fn [body] (= id (body :id)))]
    (cond (:leaf node) (let [body (:children node)]
                         (if (self? body) [] [body]))
          (far-node? node) [{:pos  (:pos node)
                            :mass (:mass node)}]
          true (mapcat #(get-clustered pos id %) (:children node)))))