(ns gravsim.lib.trafo)

(def inv #(/ 1 %))

(defn add [x1 x2] [(+ (get x1 0) (get x2 0))
                   (+ (get x1 1) (get x2 1))])

(defn sub [x1 x2] [(- (get x1 0) (get x2 0))
                   (- (get x1 1) (get x2 1))])

(defn scalar [num v] [(* num (get v 0))
                      (* num (get v 1))])

(def neg #(scalar -1 %))

(defn norm [v]
  (let [sqr #(* % %)]
    (Math/sqrt (+ (sqr (get v 0)) (sqr (get v 1))))))

(defn normalize [v]
  (scalar (inv (norm v)) v))

(defn v-dist [v1 v2]
  (let [dv (sub v1 v2)]
    (norm dv)))

(defn midpoint [v1 v2]
  (scalar 0.5 (add v1 v2)))
