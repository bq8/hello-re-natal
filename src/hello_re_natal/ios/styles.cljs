(ns hello-re-natal.ios.styles)

(def styles
  
  {:root-view {:background-color "#E3F2FD"
               :flex-direction "column"
               :padding 40
               :position "absolute"
               :top      0
               :left     0
               :bottom   0
               :right    0}
   :segmented-control {:style {:margin-top 5}
                       :tint-color "#DB5461"}
   :text-input {:style {:background-color "#FAFAFA"
                        :color "#686963"
                        :height 40
                        :margin-top 10}}
   :text-label {:style {:color "#DB5461"
                        :margin-top 10}}
   :text-output {:color "#000"}})
