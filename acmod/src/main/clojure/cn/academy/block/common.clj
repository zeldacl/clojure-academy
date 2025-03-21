(ns cn.academy.block.common
  (:require [cn.academy.api.block :as block-api]
            [clojure.tools.logging :as log]))

(defn create-block-base
  "Creates a basic block with common properties"
  [factory material-name hardness harvest-tool harvest-level]
  (let [material (-> factory
                    block-api/create-block-properties
                    (block-api/get-block-material material-name))
        block-properties (block-api/create-block-properties factory)
        container (block-api/create-block-container factory material)]
    
    (block-api/set-hardness! container hardness)
    (block-api/set-harvest-level! container harvest-tool harvest-level)
    
    {:container container
     :properties block-properties}))

(defn add-property!
  "Safely adds a property to a block container"
  [container property]
  (when property
    (try
      (.addProperty container property)
      (catch Exception e
        (log/error "Failed to add property to block:" (.getMessage e))))))

(defmacro def-block-property
  "Defines a new block property with an atom"
  [name]
  `(def ~name (atom nil)))

(defn create-boolean-property!
  "Creates and resets a boolean property"
  [property-atom properties name]
  (reset! property-atom (block-api/create-boolean-property properties name)))

(defn create-integer-property!
  "Creates and resets an integer property"
  [property-atom properties name min max]
  (reset! property-atom (block-api/create-integer-property properties name min max)))