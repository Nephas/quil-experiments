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

(defn slice-nw [rect]
  [(get rect X)
   (get rect Y)
   (half-width rect)
   (half-height rect)])

(defn slice-ne [rect]
  [(+ (get rect X) (half-width rect))
   (get rect Y)
   (half-width rect)
   (half-height rect)])

(defn slice-sw [rect]
  [(get rect X)
   (+ (get rect Y) (half-height rect))
   (half-width rect)
   (half-height rect)])

(defn slice-se [rect]
  [(+ (get rect X) (half-width rect))
   (+ (get rect Y) (half-height rect))
   (half-width rect)
   (half-height rect)])

(defn rect-contains [rect pos]
  (let [xmax (+ (get rect X) (get rect WIDTH))
        ymax (+ (get rect Y) (get rect HEIGHT))]
    (and (< (get rect X) (get pos X) xmax) (< (get rect Y) (get pos Y) ymax))))

(defn quadtree-node [rect bodies]
  (let [contained-bodies (filter (fn [body] (rect-contains rect (:pos body))) bodies)
        mass (apply + (map #(:mass %) contained-bodies))
        num (count contained-bodies)]

    (case num 0 nil
              1 {:rect rect
                 :pos  (center rect)
                 :mass mass
                 :body (first contained-bodies)}

              {:rect     rect
               :pos      (center rect)
               :mass     mass

               :child-nw (quadtree-node (slice-nw rect) contained-bodies)
               :child-ne (quadtree-node (slice-ne rect) contained-bodies)
               :child-sw (quadtree-node (slice-sw rect) contained-bodies)
               :child-se (quadtree-node (slice-se rect) contained-bodies)})))

(defn get-children [node]
  (filter some? [(:child-ne node)
                 (:child-nw node)
                 (:child-se node)
                 (:child-sw node)]))

(defn has-children [node]
  (seq (get-children node)))

(def THRESHOLD 1)

(defn get-clustered [pos node]
  (let [dist-par (/ (get (:rect node) WIDTH) (+ 1e-10 (t/v-dist pos (:pos node))))
        far-node? (< dist-par THRESHOLD)
        children (get-children node)
        has-children? (seq children)]
    (if far-node?
      (select-keys node [:pos :mass])
      (if has-children?
        (flatten (map #(get-clustered pos %) children))
        (select-keys (:body node) [:pos :mass])))))
