(ns gravsim.lib.quad-test
  (:use clojure.test)
  (:require [gravsim.lib.quad :as q]))

(deftest rect
  (testing "center method"
    (is (= (q/center [0 0 4.0 6.0]) [2.0 3.0]))
    (is (= (q/center [2.0 2.0 4.0 6.0]) [4.0 5.0]))))

(deftest grouping
  (testing "group by quad"
    (let [body1 {:pos [1 2]}
          body2 {:pos [2 3]}
          body3 {:pos [6 6]}
          body4 {:pos [8 1]}
          bodies [body1 body2 body3 body4]]
      (is (= (q/group-by-quadrant [5 4] bodies) [[body1 body2] [body4] [] [body3]])))))

(deftest quadtree
  (let [body1 {:pos [1 2] :mass 2 :id 1}
        body2 {:pos [4 4] :mass 3 :id 2}
        body3 {:pos [8 1] :mass 5 :id 3}
        rect [0 0 10 10]
        bodies [body1 body2 body3]
        actual-tree (q/quadtree-node rect bodies)
        clustered-tree (flatten (q/get-clustered [9 4] 4 actual-tree))]
    (testing "tree construction"
      (is (= body1 (get-in actual-tree [:children 0 :children 0 :bodies])))
      (is (= (:pos body1) (get-in actual-tree [:children 0 :children 0 :pos])))
      (is (= (q/get-center-of-mass body1 body2) (get-in actual-tree [:children 0 :pos])))
      (is (false? (get-in actual-tree [:leaf])))
      (is (true? (get-in actual-tree [:children 0 :children 0 :leaf])))
      (is (nil? (get-in actual-tree [:children 2])))
      (is (= body2 (get-in actual-tree [:children 0 :children 1 :bodies])))
      (is (= body3 (get-in actual-tree [:children 1 :bodies])))
      (is (= 10 (get-in actual-tree [:mass]))))
    (testing "tree clustering"
      (is (= body3 (last clustered-tree)))
      (is (= (+ (:mass body1) (:mass body2)) (:mass (first clustered-tree)))))))

(defn -main [] (run-tests 'gravsim.lib.quad-test))