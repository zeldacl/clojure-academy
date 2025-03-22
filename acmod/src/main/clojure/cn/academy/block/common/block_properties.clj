(ns cn.academy.block.common.block-properties
  (:require [cn.academy.protocols.block :as block-api]
            [clojure.tools.logging :as log]))

(defn create-boolean-state! 
  "Create a boolean property for a block state"
  [block-properties property-name]
  (block-api/create-boolean-property block-properties property-name))

(defn create-integer-state!
  "Create an integer property for a block state with min and max values"
  [block-properties property-name min max]
  (block-api/create-integer-property block-properties property-name min max))

(defn add-property!
  "Safely add a property to a block container"
  [container property]
  (when property
    (try
      (.addProperty container property)
      true
      (catch Exception e
        (log/error "Failed to add property to block:" (.getMessage e))
        false))))

(defn create-block-base
  "Create a basic block with common settings"
  [factory material-name hardness harvest-level]
  (let [factory @block-api/*forge-factory*
        material (-> factory
                    block-api/create-block-properties
                    (block-api/get-block-material material-name))
        block-properties (block-api/create-block-properties factory)
        container (block-api/create-block-container factory material)]
    
    (block-api/set-hardness! container hardness)
    (block-api/set-harvest-level! container "pickaxe" harvest-level)
    
    [container block-properties]))