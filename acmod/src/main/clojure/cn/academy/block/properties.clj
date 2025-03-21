(ns cn.academy.block.properties
  (:require [clojure.tools.logging :as log]))

(def default-properties
  {:machine {:material :iron
            :hardness 3.0
            :resistance 5.0
            :harvest-tool "pickaxe"
            :harvest-level 1}
   
   :energy-machine (merge {:material :iron
                          :hardness 4.0
                          :resistance 8.0
                          :harvest-tool "pickaxe"
                          :harvest-level 2}
                         {:has-tile-entity true})
   
   :processor (merge {:material :iron
                     :hardness 5.0 
                     :resistance 10.0
                     :harvest-tool "pickaxe"
                     :harvest-level 2}
                    {:has-tile-entity true})})

(defn get-default-properties [block-type]
  (get default-properties block-type {}))

(defn merge-properties [base & updates]
  (reduce merge base updates))

;; Block state atom management helpers
(defn create-property-atom [initial-value]
  (atom initial-value))

(defn set-property! [property-atom value]
  (reset! property-atom value))

(defn update-property! [property-atom f & args]
  (apply swap! property-atom f args))

;; Common property validation 
(defn validate-property! [property value validator]
  (try
    (validator value)
    true
    (catch Exception e
      (log/error "Property validation failed:" (.getMessage e))
      false)))