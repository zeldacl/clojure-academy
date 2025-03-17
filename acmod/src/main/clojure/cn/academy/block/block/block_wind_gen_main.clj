(ns cn.academy.block.block.block-wind-gen-main
  (:require [cn.academy.api.block :as block-api]
            [clojure.tools.logging :as log]))

;; Define the wind generator block properties
(def wind-gen-active (atom nil))

(defn init-properties! [block-properties]
  (reset! wind-gen-active (block-api/create-boolean-property block-properties "active")))

;; Create the wind generator block
(defn create-wind-gen-main []
  (let [factory @block-api/*forge-factory*
        material (-> factory
                    block-api/create-block-properties
                    (block-api/get-block-material "iron"))
        block-properties (block-api/create-block-properties factory)
        container (block-api/create-block-container factory material)]
    
    ;; Initialize properties if needed
    (when (nil? @wind-gen-active)
      (init-properties! block-properties))
    
    ;; Set basic block properties
    (block-api/set-hardness! container 4.0)
    (block-api/set-harvest-level! container "pickaxe" 2)
    
    ;; Add properties
    (when @wind-gen-active
      (try
        (.addProperty container @wind-gen-active)
        (catch Exception e
          (log/error "Failed to add properties to wind generator block:" (.getMessage e)))))
    
    container))

;; Helper functions for block state handling
(defn is-active? [state]
  (when @wind-gen-active
    (block-api/get-property state @wind-gen-active)))

(defn set-active [state active]
  (when @wind-gen-active
    (block-api/with-property state @wind-gen-active active)))

;; Export the constructor function for Java interop
(gen-class
  :name cn.academy.block.block.BlockWindGenMain$Factory
  :methods [^:static [create [] Object]
            ^:static [initProperties [Object] void]]
  :prefix "block-factory-")

(defn block-factory-create []
  (create-wind-gen-main))

(defn block-factory-initProperties [block-properties]
  (init-properties! block-properties))