(ns cn.academy.block.block.block-metal-former
  (:require [cn.academy.api.block :as block-api]
            [clojure.tools.logging :as log]))

;; Define the metal former block properties
(def metal-former-active (atom nil))
(def metal-former-facing (atom nil))

(defn init-properties! [block-properties]
  (reset! metal-former-active (block-api/create-boolean-property block-properties "active"))
  (reset! metal-former-facing (block-api/create-integer-property block-properties "facing" 0 5)))

;; Create the metal former block
(defn create-metal-former []
  (let [factory @block-api/*forge-factory*
        material (-> factory
                    block-api/create-block-properties
                    (block-api/get-block-material "iron"))
        block-properties (block-api/create-block-properties factory)
        container (block-api/create-block-container factory material)]
    
    ;; Initialize properties if needed
    (when (nil? @metal-former-active)
      (init-properties! block-properties))
    
    ;; Set basic block properties
    (block-api/set-hardness! container 3.0)
    (block-api/set-harvest-level! container "pickaxe" 1)
    
    ;; Add properties
    (when (and @metal-former-active @metal-former-facing)
      (try
        (.addProperty container @metal-former-active)
        (.addProperty container @metal-former-facing)
        (catch Exception e
          (log/error "Failed to add properties to metal former block:" (.getMessage e)))))
    
    container))

;; Helper functions for block state handling
(defn is-active? [state]
  (when @metal-former-active
    (block-api/get-property state @metal-former-active)))

(defn set-active [state active]
  (when @metal-former-active
    (block-api/with-property state @metal-former-active active)))

(defn get-facing [state]
  (when @metal-former-facing
    (block-api/get-property state @metal-former-facing)))

(defn set-facing [state facing]
  (when @metal-former-facing
    (block-api/with-property state @metal-former-facing facing)))

;; Export the constructor function for Java interop
(gen-class
  :name cn.academy.block.block.BlockMetalFormer$Factory
  :methods [^:static [create [] Object]
            ^:static [initProperties [Object] void]]
  :prefix "block-factory-")

(defn block-factory-create []
  (create-metal-former))

(defn block-factory-initProperties [block-properties]
  (init-properties! block-properties))