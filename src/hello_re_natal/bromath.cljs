(ns hello-re-natal.bromath)

(def pounds-per-kilogram 0.45359237)
(defn to-pounds
  [weight]
  (/ weight pounds-per-kilogram))
(defn to-kilos
  [weight]
  (* weight pounds-per-kilogram))

(defrecord Barbell [weight display-string])
(def barbells [(Barbell. 20 "Mens 20kg/44lb Olympic") (Barbell. 15 "Womens 15kg/33lb Olympic")])

(defrecord Disc-Set [discs unit display-string])
(def discs [(Disc-Set. [25 20 15 10 5 2.5] "kg" "Kilograms") (Disc-Set. [45 35 25 10 5 2.5] "lb" "Pounds")])

(defn baz [accum plate-weights remaining-weight]
  (if (or (<= remaining-weight 0) 
          (empty? plate-weights))
    accum
    (recur 
      (assoc accum (keyword (str "disc-" (first plate-weights))) (quot remaining-weight (first plate-weights)))
      (rest plate-weights)
      (- remaining-weight (* (first plate-weights) (quot remaining-weight (first plate-weights)))))))

(defn calc-weight 
  [target-weight target-weight-unit barbell-weight disc-unit disc-set]
  (let [target-weight (cond
                        (= target-weight-unit disc-unit) target-weight
                        (zero? disc-unit) (to-kilos target-weight)
                        :else (to-pounds target-weight))
        weight-per-side (/ (- target-weight barbell-weight) 2)]
    (filter (fn [x]
              (pos? (val x))) 
            (baz {} disc-set weight-per-side))))
