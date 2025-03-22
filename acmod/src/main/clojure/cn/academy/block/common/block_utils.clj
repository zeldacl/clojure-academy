(ns cn.academy.block.common.block-utils
  (:require [cn.academy.protocols.block :as block-api]
            [clojure.tools.logging :as log]))

(defn create-boolean-state-property
  "Create a boolean state property with given name"
  [block-properties property-name]
  (block-api/create-boolean-property block-properties property-name))

(defn create-basic-block
  "Create a basic block with common settings"
  [hardness harvest-level]
  (let [factory @block-api/*forge-factory*
        material (-> factory
                    block-api/create-block-properties
                    (block-api/get-block-material "iron"))
        block-properties (block-api/create-block-properties factory)
        container (block-api/create-block-container factory material)]
    
    (block-api/set-hardness! container hardness)
    (block-api/set-harvest-level! container "pickaxe" harvest-level)
    
    {:container container
     :properties block-properties}))

(defn add-property!
  "Safely add a property to a block container"
  [container property block-name]
  (try
    (.addProperty container property)
    (catch Exception e
      (log/error (str "Failed to add property to " block-name " block:") (.getMessage e)))))

(defn create-block-state-handlers
  "Create standard get/set state handlers for a property"
  [property]
  {:get-state (fn [state]
                (when property
                  (block-api/get-property state property)))
   :set-state (fn [state value]
                (when property
                  (block-api/with-property state property value)))})