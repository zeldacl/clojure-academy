(ns cn.academy.block.block.block-phase-gen
  (:require [cn.academy.api.block :as block-api]
            [clojure.tools.logging :as log]))

;; Define the phase generator block properties
(def phase-gen-active (atom nil))

(defn init-properties! [block-properties]
  (reset! phase-gen-active (block-api/create-boolean-property block-properties "active")))

;; Create the phase generator block
(defn create-phase-gen []
  (let [factory @block-api/*forge-factory*
        material (-> factory
                    block-api/create-block-properties
                    (block-api/get-block-material "iron"))
        block-properties (block-api/create-block-properties factory)
        container (block-api/create-block-container factory material)]
    
    ;; Initialize properties if needed
    (when (nil? @phase-gen-active)
      (init-properties! block-properties))
    
    ;; Set basic block properties
    (block-api/set-hardness! container 2.5)
    (block-api/set-harvest-level! container "pickaxe" 1)
    
    ;; Add properties
    (when @phase-gen-active
      (try
        (.addProperty container @phase-gen-active)
        (catch Exception e
          (log/error "Failed to add properties to phase generator block:" (.getMessage e)))))
    
    container))

;; Helper functions for block state handling
(defn is-active? [state]
  (when @phase-gen-active
    (block-api/get-property state @phase-gen-active)))

(defn set-active [state active]
  (when @phase-gen-active
    (block-api/with-property state @phase-gen-active active)))

;; Export the constructor function for Java interop
(gen-class
  :name cn.academy.block.block.BlockPhaseGen$Factory
  :methods [^:static [create [] Object]
            ^:static [initProperties [Object] void]]
  :prefix "block-factory-")

(defn block-factory-create []
  (create-phase-gen))

(defn block-factory-initProperties [block-properties]
  (init-properties! block-properties))