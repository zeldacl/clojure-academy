(ns cn.academy.forge.v1_16_5.matrix
  (:require [cn.academy.block.matrix.core :as matrix]
            [cn.academy.forge.v1_16_5.bridge.matrix :as matrix-bridge]
            [cn.academy.forge.v1_16_5.bridge.registry :as registry-bridge])
  (:import [net.minecraft.block AbstractBlock$Properties]
           [net.minecraft.block.material Material]
           [net.minecraft.item Item$Properties]))

;; Constants for matrix registration
(def ^:const MATRIX_BLOCK_ID "matrix")
(def ^:const MATRIX_NODE_BLOCK_ID "matrix_node")
(def ^:const MATRIX_CORE_BLOCK_ID "matrix_core")

;; Function to set up matrix block properties
(defn create-matrix-properties []
  {:material :metal
   :hardness 3.5
   :resistance 10.0
   :light-level 7
   :capabilities {:energy true :inventory true}})

;; Function to register all matrix blocks
(defn register-matrix-blocks []
  (let [matrix-props (create-matrix-properties)
        core-props (assoc matrix-props :light-level 15)
        node-props (assoc matrix-props :light-level 5)]
    
    ;; Register main matrix block
    (matrix-bridge/register-matrix-component 
     registry-bridge/blocks-registry
     MATRIX_BLOCK_ID
     matrix-props)
    
    ;; Register matrix core block
    (matrix-bridge/register-matrix-component
     registry-bridge/blocks-registry
     MATRIX_CORE_BLOCK_ID
     core-props)
    
    ;; Register matrix node block
    (matrix-bridge/register-matrix-component
     registry-bridge/blocks-registry
     MATRIX_NODE_BLOCK_ID
     node-props)))

;; Hook for integrating matrix components
(defn init-matrix []
  (register-matrix-blocks))