(ns hello-re-natal.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [hello-re-natal.events]
            [hello-re-natal.subs]
            [cljs.reader :as reader]
            [hello-re-natal.ios.styles :as s]
            [hello-re-natal.bromath :as bruh]))

(def ReactNative (js/require "react-native"))
(def app-registry (.-AppRegistry ReactNative))
(def dimensions (.-Dimensions ReactNative))
(def segmented-control (r/adapt-react-class (.-SegmentedControlIOS ReactNative)))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def text-input (r/adapt-react-class (.-TextInput ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))

;{:keys [width height]} (js->clj (.get dimensions "window") :keywordize-keys true)

(defn get-dimensions [name]
  (js->clj (.get dimensions name) :keywordize-keys true))

(defn app-root []
  (let [text-input-state (atom "")
        target-weight-state (atom 0)
        target-weight-unit-state (atom 0)
        barbell-type-state (atom 0)
        disc-unit-state (atom 0)
        window-width (:width (get-dimensions "window"))
        disc-decoration-width (* 0.3 window-width)]
    (fn []
      [view {:style (get-in s/styles [:root-view])}
       [view {:style {:flex-direction "row"
                      :flex-wrap "wrap"
                      :justify-content "center"}}
        [view {:style {:background-color "#FF595E"
                       :border-radius (* 0.5 disc-decoration-width)
                       :height disc-decoration-width
                       :margin 5
                       :width disc-decoration-width}}]
        [view {:style {:background-color "#2176AE"
                       :border-radius (* 0.5 disc-decoration-width)
                       :height disc-decoration-width
                       :margin 5
                       :width disc-decoration-width}}]
        [view {:style {:background-color "#F3D34A"
                       :border-radius (* 0.5 disc-decoration-width)
                       :height disc-decoration-width
                       :margin 5
                       :width disc-decoration-width}}]
        [view {:style {:background-color "#76A530"
                       :border-radius (* 0.5 disc-decoration-width)
                       :height disc-decoration-width
                       :margin 5
                       :width disc-decoration-width}}]]
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
                           :values (map #(:display-string %) bruh/barbells)}]
       [text {:style (get-in s/styles [:text-label :style])} "What kind of weights will you be using?"]
       [segmented-control {:on-change #(reset! disc-unit-state (-> % .-nativeEvent .-selectedSegmentIndex))
                           :selected-index @disc-unit-state
                           :style (get-in s/styles [:segmented-control :style])
                           :tint-color (get-in s/styles [:segmented-control :tint-color])
                           :values (map #(:display-string %) bruh/discs)}]
       [text {:style (get-in s/styles [:text-output])} 
        (if (number? (reader/read-string @text-input-state)) 
          (str (bruh/calc-weight 
                 @target-weight-state 
                 @target-weight-unit-state 
                 (cond-> (:weight (bruh/barbells @barbell-type-state)) (pos? @disc-unit-state) bruh/to-pounds)
                 @disc-unit-state 
                 (:discs (bruh/discs @disc-unit-state)))) 
          (str "Not a number: " @text-input-state @target-weight-state))]])))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "HelloReNatal" #(r/reactify-component app-root)))
