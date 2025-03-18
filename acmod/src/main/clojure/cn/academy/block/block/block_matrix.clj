(ns cn.academy.block.block.block-matrix
  (:require [cn.academy.block.registration :as reg]))

;; Define matrix block properties
(def block-matrix-def
  {:properties {:material "rock"
               :hardness 3.0 
               :resistance 3.0
               :light-level 1.0
               :has-tile-entity true}
   :event-handlers {:on-block-activated (fn [world pos state player]
                                        ;; TODO: Implement matrix block activation logic
                                        true)
                   :on-block-placed (fn [world pos state placer stack]
                                    ;; TODO: Implement matrix block placement logic
                                    nil)}})

(defn register! [registration mod-id block-id block-def]
  "Register the matrix block with the given registration implementation"
  (reg/register-block! registration block-def mod-id block-id))