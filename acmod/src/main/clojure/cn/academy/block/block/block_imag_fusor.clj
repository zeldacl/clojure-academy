(ns cn.academy.block.block.block-imag-fusor
  (:require [cn.academy.api.block :as block-api]
            [clojure.tools.logging :as log]))

;; Define the imag fusor block properties
(def imag-fusor-active (atom nil))
(def imag-fusor-facing (atom nil))

(defn init-properties! [block-properties]
  (reset! imag-fusor-active (block-api/create-boolean-property block-properties "active"))
  (reset! imag-fusor-facing (block-api/create-integer-property block-properties "facing" 0 5)))

;; Create the imag fusor block
(defn create-imag-fusor []
  (let [factory @block-api/*forge-factory*
        material (-> factory
                    block-api/create-block-properties
                    (block-api/get-block-material "iron"))
        block-properties (block-api/create-block-properties factory)
        container (block-api/create-block-container factory material)]
    
    ;; Initialize properties if needed
    (when (nil? @imag-fusor-active)
      (init-properties! block-properties))
    
    ;; Set basic block properties
    (block-api/set-hardness! container 3.0)
    (block-api/set-harvest-level! container "pickaxe" 1)
    
    ;; Add properties
    (when (and @imag-fusor-active @imag-fusor-facing)
      (try
        (.addProperty container @imag-fusor-active)
        (.addProperty container @imag-fusor-facing)
        (catch Exception e
          (log/error "Failed to add properties to imag fusor block:" (.getMessage e)))))
    
    container))

;; Helper functions for block state handling
(defn is-active? [state]
  (when @imag-fusor-active
    (block-api/get-property state @imag-fusor-active)))

(defn set-active [state active]
  (when @imag-fusor-active
    (block-api/with-property state @imag-fusor-active active)))

(defn get-facing [state]
  (when @imag-fusor-facing
    (block-api/get-property state @imag-fusor-facing)))

(defn set-facing [state facing]
  (when @imag-fusor-facing
    (block-api/with-property state @imag-fusor-facing facing)))

;; Export the constructor function for Java interop
(gen-class
  :name cn.academy.block.block.BlockImagFusor$Factory
  :methods [^:static [create [] Object]
            ^:static [initProperties [Object] void]]
  :prefix "block-factory-")

(defn block-factory-create []
  (create-imag-fusor))

(defn block-factory-initProperties [block-properties]
  (init-properties! block-properties))