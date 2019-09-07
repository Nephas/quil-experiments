(ns gravsim_clj.quad
  (:require [gravsim_clj.trafo :as t]))

(def X 0)
(def Y 1)

(def WIDTH 2)
(def HEIGHT 3)

(defn half-width [rect] (* 0.5 (get rect WIDTH)))
(defn half-height [rect] (* 0.5 (get rect HEIGHT)))

(defn center [rect]
  [(+ (get rect X) (half-width rect))
   (+ (get rect Y) (half-height rect))])

(defn area [rect] (* (get rect WIDTH) (get rect HEIGHT)))

(defn slice [rect [center-x center-y] direction]
  (let [height (half-width rect)
        width (half-height rect)]
    (case direction :nw [(get rect X) (get rect Y) height width]
                    :ne [center-x (get rect Y) height width]
                    :sw [(get rect X) center-y height width]
                    :se [center-x center-y height width])))

(defn determine-quad [[x y] [center-x center-y]]
  (let [west? (< x center-x)
        north? (< y center-y)]
    (cond (and west? north?) :nw
          (and (not west?) (not north?)) :se
          west? :sw
          north? :ne)))

(defn quadtree-node [rect bodies]
  (let [mass (apply + (map #(:mass %) bodies))
        center (center rect)
        num (count bodies)]

    (cond (zero? num) nil
          (= num 1) {:rect   rect
                     :pos    center
                     :mass   mass
                     :density (/ mass (area rect))
                     :bodies bodies}

          true (let [grouped (group-by #(determine-quad (:pos %) center) bodies)
                     children (filter some?
                                      (map (fn [dir] (quadtree-node (slice rect center dir)
                                                                    (dir grouped)))
                                           [:nw :ne :sw :se]))]
                 {:rect     rect
                  :pos      center
                  :mass     mass
                  :density (/ mass (area rect))
                  :children children}))))

(def THRESHOLD 1)

(defn get-clustered [pos node]
  (let [dist-par (/ (get (:rect node) WIDTH) (+ 1e-10 (t/v-dist pos (:pos node))))
        far-node? (< dist-par THRESHOLD)
        children (:children node)
        has-children? (seq children)]
    (cond far-node? (select-keys node [:pos :mass])
          has-children? (flatten (map #(get-clustered pos %) children))
          true (:bodies node))))