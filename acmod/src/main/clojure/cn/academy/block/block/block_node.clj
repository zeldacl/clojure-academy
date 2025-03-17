(ns cn.academy.block.block.block-node
  (:require [clojure.tools.logging :as log]
            [cn.academy.api.block :as block-api])
  (:import [cn.academy.api.block IForgeBlockFactory IBlockProperties IBlockContainer]))

;; Get the factory instance
(def forge-factory (atom nil))

(defn set-forge-factory! [factory]
  (reset! forge-factory factory))

;; Get properties system
(def block-properties (atom nil))

(defn init-properties! []
  (when-let [factory @forge-factory]
    (reset! block-properties (.createBlockProperties factory))))

;; Define the properties using abstraction
(def connected (atom nil))
(def energy (atom nil))

(defn init-block-properties! []
  (when-let [props @block-properties]
    (reset! connected (.createBooleanProperty props "connected"))
    (reset! energy (.createIntegerProperty props "energy" 0 4))))

;; Define the NodeType record
(defrecord NodeType [name max-energy bandwidth range capacity]
  Object
  (toString [_] name))

;; Define the node types
(def node-types 
  {:basic (->NodeType "basic" 15000 150 9 5)
   :standard (->NodeType "standard" 50000 300 12 10)
   :advanced (->NodeType "advanced" 200000 900 19 20)})

;; Create block using abstraction
(defn create-block-node []
  (let [material (-> (block-api/*forge-factory* :create-block-properties)
                    (block-api/get-block-material "rock"))
        container (-> (block-api/*forge-factory* :create-block-container material))]
    
    ;; Set basic block properties
    (block-api/set-hardness! container 3.0)
    (block-api/set-harvest-level! container "pickaxe" 1)
    
    ;; Add our custom properties
    (doto container
      (block-api/add-property node-active)
      (block-api/add-property node-level))
    
    container))

(defn get-node-level [state]
  (block-api/get-property state node-level))

(defn set-node-level [state level]
  (block-api/with-property state node-level level))

(defn is-node-active? [state]
  (block-api/get-property state node-active))

(defn set-node-active [state active]
  (block-api/with-property state node-active active))

;; Factory function to create block instances
(defn block-node [node-type]
  (create-block-node node-type))

;; Export the constructor functions for Java interop
(gen-class
  :name cn.academy.block.block.BlockNode$Factory
  :methods [^:static [createBasic [] Object]
            ^:static [createStandard [] Object]
            ^:static [createAdvanced [] Object]
            ^:static [setForgeFactory [cn.academy.api.block.IForgeBlockFactory] void]]
  :prefix "block-factory-")

(defn block-factory-setForgeFactory [factory]
  (set-forge-factory! factory)
  (init-properties!)
  (init-block-properties!))

(defn block-factory-createBasic []
  (block-node :basic))

(defn block-factory-createStandard []
  (block-node :standard))

(defn block-factory-createAdvanced []
  (block-node :advanced))