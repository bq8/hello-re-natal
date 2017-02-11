(ns hello-re-natal.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [hello-re-natal.events]
            [hello-re-natal.subs]
            [cljs.reader :as reader]
            [hello-re-natal.ios.styles :as s]))

(def read-string reader/read-string)
(def ReactNative (js/require "react-native"))

(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def text-input (r/adapt-react-class (.-TextInput ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight ReactNative)))
(def pan-responder (r/adapt-react-class (.-PanResponder ReactNative)))
(def animated (r/adapt-react-class (.-Animated ReactNative)))
(def dimensions (r/adapt-react-class (.-Dimensions ReactNative)))
(def segmented-control (r/adapt-react-class (.-SegmentedControlIOS ReactNative)))

(def logo-img (js/require "./images/cljs.png"))

(defn alert [title]
  (.alert (.-Alert ReactNative) title))

(def pounds-per-kilogram 0.45359237)
(defn to-pounds
  [weight]
  (* weight pounds-per-kilogram))

(def barbell {:mens 20 :womens 15})
(def pound-plates [45 35 25 10 5 2.5])
(def kilogram-plates [25 20 15 10 5 2.5])

;; Calculates the number of each disc required to reach a given weight.
(defn baz [accum plate-weights remaining-weight]
  (if (or (<= remaining-weight 0) 
          (empty? plate-weights))
    accum
    (recur 
      (assoc accum (keyword (str "disc-" (first plate-weights))) (quot remaining-weight (first plate-weights)))
      (rest plate-weights)
      (- remaining-weight (* (first plate-weights) (quot remaining-weight (first plate-weights)))))))

;; Calculates the number of discs required to reach a given weight, accounting for the bar.
;;
;; target-weight the total weight on the bar
;; barbell-type 20kg or 45lbs
;; pound-plates or kilogram-plates
(defn calc-weight 
  [target-weight barbell-weight weight-set]
  (let [weight-per-side (/ (- target-weight barbell-weight) 2)]
    (filter (fn [x]
              (pos? (val x))) 
            (baz {} weight-set weight-per-side))))

;;
;;

(defn app-root []
  (let [greeting (subscribe [:get-greeting])
        state (atom "")
        desired-units (atom 0)
        target-weight-unit (atom 0)
        target-weight (atom 0)
        disc-unit (atom 0)]
    (fn []
      [view {:style {:flex-direction "column"
                     :padding 40
                     :background-color "#2176ae"}} ;; 56cbf9
       [text {:style {:font-size 30
                      :font-weight "100"
                      :margin-bottom 20
                      :text-align "center"}} @greeting]
       [image {:source logo-img
               :style {:align-self "center"
                       :width 80
                       :height 80
                       :margin-bottom 30}}]
       [text {:style (get-in s/styles [:text-label])} "How much weight do you want to lift?"]
      [text-input {:style {:height 40
                           :color "#ff729f"}
                   :keyboard-type "numeric"
                   :on-change-text #(do
                                      (reset! state %)
                                      (r/flush))
                   :placeholder "Weight"
                   :return-key-type "done"
                   :value @state}]
       [segmented-control {:on-change #(do 
                                         (reset! target-weight-unit (-> % .-nativeEvent .-selectedSegmentIndex))
                                         ;; fixme: reset target weight here
                                         )
                           :selected-index @target-weight-unit
                           :tint-color (get-in s/styles [:segmented-control :tint-color])
                           :values ["kg", "lbs"]}]
       [text {:style (get-in s/styles [:text-label])} "What kind of bar do you have?"]
       [segmented-control {:tint-color (get-in s/styles [:segmented-control :tint-color])
                           :values ["Mens 20kg/44lb Olympic", "Womens 15kg/33lb Olympic"]}]
       [text {:style (get-in s/styles [:text-label])} "What kind of weights will you be using?"]
       [segmented-control {:on-change #(reset! disc-unit (-> % .-nativeEvent .-selectedSegmentIndex))
                           :tint-color (get-in s/styles [:segmented-control :tint-color])
                           :values ["kilogram discs", "pound discs"]}]
       [text {:style {:color "black"
                      :text-align "center"
                      :font-weight "bold"}}
                      (if (number? (read-string @state))
                          (str (calc-weight @state 
                                            (if (zero? @disc-unit) 20 45) 
                                            (if (zero? @disc-unit) kilogram-plates pound-plates)))
                          (str "Not a number" @state (rand 10))
                          )]])))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "HelloReNatal" #(r/reactify-component app-root)))
