(ns conway.lib.rules
  (:require [conway.lib.grid :as g]))

(defn evolve-cell [alive neighbors]
  (let [neighbor-count (count (filter true? neighbors))]
    (cond (and alive (<= 2 neighbor-count 3)) true
          (and (not alive) (= neighbor-count 3)) true
          true false)))

(defn evolve-grid [grid]
  (loop [positions (keys grid)
         next {}]
    (if (empty? positions) next
                           (let [pos (first positions)
                                 old-cell (get grid pos)
                                 neighbors (vals (select-keys grid (g/get-neighborhood pos)))]
                             (recur (rest positions)
                                    (assoc next pos (evolve-cell old-cell neighbors)))))))

