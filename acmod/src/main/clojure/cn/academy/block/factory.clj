(ns cn.academy.block.factory
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core]
            [clojure.tools.logging :as log]))

(defrecord AcademyBlock [base-properties custom-handlers]
  IBlock
  (get-properties [this]
    base-properties)
  
  (on-placed [this world pos placer]
    (when-let [handler (:on-placed custom-handlers)]
      (handler this world pos placer)))
      
  (on-broken [this world pos]
    (when-let [handler (:on-broken custom-handlers)]
      (handler this world pos)))
      
  (on-activated [this world pos player hand]
    (when-let [handler (:on-activated custom-handlers)]
      (handler this world pos player hand))))

(defn create-block
  "Create a block with given properties and optional event handlers"
  [properties handlers]
  (->AcademyBlock properties handlers))

(defn create-simple-block
  "Create a basic block with only properties"
  [properties]
  (create-block properties {}))

(defn create-interactive-block
  "Create a block that can be interacted with"
  [properties activate-handler]
  (create-block properties 
                {:on-activated activate-handler}))

(defn create-stateful-block
  "Create a block that maintains state"
  [properties handlers]
  (create-block properties handlers))

;; Default properties maps for different block types
(def machine-block-props
  {:material :iron
   :hardness 5.0
   :resistance 10.0
   :light-level 0
   :requires-tool true})
   
(def device-block-props  
  {:material :iron
   :hardness 3.0
   :resistance 5.0
   :light-level 0})
   
(def energy-block-props
  {:material :iron
   :hardness 4.0
   :resistance 8.0
   :light-level 7})