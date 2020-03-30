(ns dummy.lib.rules
  (:require [dummy.lib.grid :as g]))

(defn evolve-cell [cell neighbors]
  (let [neighbor-count (count (filter (fn [n] (:alive n)) neighbors))]
    (cond (and (:alive cell) (<= 2 neighbor-count 3)) {:alive true}
          (and (not (:alive cell)) (= neighbor-count 3)) {:alive true}
          true {:alive false})))

(defn evolve-grid [grid]
  (loop [positions (keys grid)
         next {}]
    (if (empty? positions) next
                           (let [pos (first positions)
                                 old-cell (get grid pos)
                                 neighbors (vals (select-keys grid (g/get-neighborhood pos)))]
                             (recur (rest positions)
                                    (assoc next pos (evolve-cell old-cell neighbors)))))))

