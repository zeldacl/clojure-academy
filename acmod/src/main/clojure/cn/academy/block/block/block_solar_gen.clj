(ns cn.academy.block.block.block-solar-gen
  (:require [cn.academy.protocols.block :as block-api]
            [cn.academy.energy.api :as energy]
            [clojure.tools.logging :as log]))

;; Define the solar generator block properties
(def solar-gen-active (atom nil))

(defn init-properties! [block-properties]
  (reset! solar-gen-active (block-api/create-boolean-property block-properties "active")))

;; Create the solar generator block
(defn create-solar-gen []
  (let [factory @block-api/*forge-factory*
        material (-> factory
                    block-api/create-block-properties
                    (block-api/get-block-material "iron"))
        block-properties (block-api/create-block-properties factory)
        container (block-api/create-block-container factory material)]
    
    ;; Initialize properties if needed
    (when (nil? @solar-gen-active)
      (init-properties! block-properties))
    
    ;; Set basic block properties
    (block-api/set-hardness! container 1.5)
    (block-api/set-harvest-level! container "pickaxe" 1)
    
    ;; Add properties
    (when @solar-gen-active
      (try
        (.addProperty container @solar-gen-active)
        (catch Exception e
          (log/error "Failed to add properties to solar generator block:" (.getMessage e)))))
    
    container))

;; Helper functions for block state handling
(defn is-active? [state]
  (when @solar-gen-active
    (block-api/get-property state @solar-gen-active)))

(defn set-active [state active]
  (when @solar-gen-active
    (block-api/with-property state @solar-gen-active active)))

;; Export the constructor function for Java interop
(gen-class
  :name cn.academy.block.block.BlockSolarGen$Factory
  :methods [^:static [create [] Object]
            ^:static [initProperties [Object] void]]
  :prefix "block-factory-")

(defn block-factory-create []
  (create-solar-gen))

(defn block-factory-initProperties [block-properties]
  (init-properties! block-properties))