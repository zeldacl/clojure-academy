(ns cn.academy.registration
  (:require [mcmod.protocols :refer :all]
            [mcmod.registry :as mcr]
            [mcmod.util :as util]
            [cn.academy.block.block-node :as block-node]
            [cn.academy.block.tileentity.tile-node :as tile-node]
            [clojure.tools.logging :as log]))

(def MOD-ID "acmod")

;; Registry instance that will hold all mod content
(def registry (atom nil))

;; Initialize registry on first use
(defn get-registry []
  (when (nil? @registry)
    (reset! registry (mcr/create-registry MOD-ID)))
  @registry)

;; Block registration helpers
(defn register-block! [block-id block]
  (let [reg (get-registry)]
    (mcr/register-block! reg block-id block)))

(defn register-tile-entity! [block-id te-def]
  (let [reg (get-registry)]
    (mcr/register-tile-entity! reg block-id te-def)))

;; Register wireless nodes
(defn register-nodes! []
  ;; Register basic node
  (register-block! "node_basic" (block-node/create-basic-node))
  (register-tile-entity! "node_basic" #(tile-node/create-node-tile :basic))

  ;; Register standard node
  (register-block! "node_standard" (block-node/create-standard-node))
  (register-tile-entity! "node_standard" #(tile-node/create-node-tile :standard))

  ;; Register advanced node
  (register-block! "node_advanced" (block-node/create-advanced-node))
  (register-tile-entity! "node_advanced" #(tile-node/create-node-tile :advanced)))

;; Core initialization function
(defn init-registration! []
  (log/info "Initializing AcademyCraft registration")
  
  ;; Register wireless nodes
  (register-nodes!)
  
  (log/info "AcademyCraft registration complete"))