(ns cn.academy.block.common.block-base
  (:require [cn.academy.api.block :as block-api]
            [clojure.tools.logging :as log]))

(defn create-block-property [block-properties name property-type & args]
  (try
    (case property-type
      :boolean (block-api/create-boolean-property block-properties name)
      :integer (apply block-api/create-integer-property block-properties name args)
      nil)
    (catch Exception e
      (log/error "Failed to create block property:" name (.getMessage e))
      nil)))

(defn create-base-block [factory material-name hardness harvest-level]
  (let [material (-> factory
                    block-api/create-block-properties
                    (block-api/get-block-material material-name))
        block-properties (block-api/create-block-properties factory)
        container (block-api/create-block-container factory material)]
    
    (block-api/set-hardness! container hardness)
    (block-api/set-harvest-level! container "pickaxe" harvest-level)
    
    {:container container
     :properties block-properties}))

(defn add-block-property! [container property]
  (when property
    (try
      (.addProperty container property)
      true
      (catch Exception e
        (log/error "Failed to add property to block:" (.getMessage e))
        false))))