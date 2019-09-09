(ns gravsim.lib.rand)

(declare rand-n)

(def seed (atom 0))

(def pars {:size (Math/pow 2 32)
           :mult 1664525
           :inc  1013904223})

(defn next-linear-congruential []
  (let [n @seed
        {size :size
         mult :mult
         inc  :inc} pars]
    (reset! seed (mod (+ (* mult n) inc) size))))

(defn set-seed! [num]
  "Sets the seed of the global random number generator."
  (reset! seed num))

(defn uniform
  "Returns a random floating point number between 0 (inclusive) and
  n (default 1) (exclusive)."
  ([] (/ (next-linear-congruential) (:size pars)))
  ([x] (* x (uniform)))
  ([x1 x2] (let [diff (- x1 x2)]
             (+ x2 (uniform diff)))))

(defn phase
  "Return a uniform value between 0 and 2 PI"
  [] (uniform (* 2 Math/PI)))

(defn rand-n
  "Returns a random integer between 0 (inclusive) and n (exclusive)."
  ([n] (int (uniform n)))
  ([n1 n2] (int (uniform n1 n2))))

(defn rand-coll
  "Return a random element of a (sequential) collection."
  [coll] (nth coll (rand-n (count coll))))

(defn rand-bool []
  (zero? (rand-n 2)))

(defn new-seed []
  (rand-n 1000000 100000000))

(defn rand-cdf
  "Given an ordered list of value->cumulative probability pairs, returns a random value."
  ([cdf-tuples]
   (let [r (uniform)
         rand-threshold #(< r (last %))]
     (first (first (filter rand-threshold cdf-tuples)))))
  ([vals cdf] (rand-cdf (zipmap vals cdf))))

(defn poisson [λ]
  "for λ > 10: roughly gaussian centered at λ"
  (loop [p 1
         k 0]
    (if (< p (Math/exp (- λ))) (dec k)
                               (let [u (uniform)] (recur (* p u) (inc k))))))

(defn rand-gauss
  ([] (/ (+ (uniform -0.5 0.5) (- (poisson 10) 10)) 5))
  ([width] (* width (rand-gauss))))
