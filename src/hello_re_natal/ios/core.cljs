(ns hello-re-natal.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [hello-re-natal.events]
            [hello-re-natal.subs]
            [cljs.reader :as reader]
            [hello-re-natal.ios.styles :as s]))

(def ReactNative (js/require "react-native"))
(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def text-input (r/adapt-react-class (.-TextInput ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def segmented-control (r/adapt-react-class (.-SegmentedControlIOS ReactNative)))

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

(defn app-root []
  (let [text-input-state (atom "")
        target-weight-state (atom 0)
        target-weight-unit-state (atom 0)
        barbell-type-state (atom 0)
        disc-unit-state (atom 0)]
    (fn []
      [view {:style (get-in s/styles [:root-view])}
       [text {:style (get-in s/styles [:text-label :style])} "How much weight do you want to lift?"]
       [text-input {:style (get-in s/styles [:text-input :style])
                    :keyboard-type "numeric"
                    :on-change-text #(do
                                       (reset! text-input-state %)
                                       (reset! target-weight-state 
                                               (if (number? (reader/read-string @text-input-state))
                                                 @text-input-state
                                                 nil))
                                       (r/flush))
                    :placeholder "Weight"
                    :return-key-type "done"
                    :value @text-input-state}]
       [segmented-control {:on-change #(reset! target-weight-unit-state (-> % .-nativeEvent .-selectedSegmentIndex))
                           :selected-index @target-weight-unit-state
                           :style (get-in s/styles [:segmented-control :style])
                           :tint-color (get-in s/styles [:segmented-control :tint-color])
                           :values ["kg", "lbs"]}]
       [text {:style (get-in s/styles [:text-label :style])} "What kind of bar do you have?"]
       [segmented-control {:on-change #(reset! barbell-type-state (-> % .-nativeEvent .-selectedSegmentIndex))
                           :selected-index @barbell-type-state
                           :style (get-in s/styles [:segmented-control :style])
                           :tint-color (get-in s/styles [:segmented-control :tint-color])
                           :values (map #(:display-string %) barbells)}]
       [text {:style (get-in s/styles [:text-label :style])} "What kind of weights will you be using?"]
       [segmented-control {:on-change #(reset! disc-unit-state (-> % .-nativeEvent .-selectedSegmentIndex))
                           :selected-index @disc-unit-state
                           :style (get-in s/styles [:segmented-control :style])
                           :tint-color (get-in s/styles [:segmented-control :tint-color])
                           :values (map #(:display-string %) discs)}]
       [text {:style (get-in s/styles [:text-output])} 
        (if (number? (reader/read-string @text-input-state)) 
          (str (calc-weight 
                 @target-weight-state 
                 @target-weight-unit-state 
                 (cond-> (:weight (barbells @barbell-type-state)) (pos? @disc-unit-state) to-pounds)
                 @disc-unit-state 
                 (:discs (discs @disc-unit-state)))) 
          (str "Not a number: " @text-input-state @target-weight-state))]])))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "HelloReNatal" #(r/reactify-component app-root)))
