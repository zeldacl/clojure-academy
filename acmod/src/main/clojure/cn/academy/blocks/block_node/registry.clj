(ns cn.academy.blocks.block-node.registry
  (:require [cn.academy.blocks.block-node.core :as core]
            [cn.academy.blocks.block-node.tile :as tile]
            [cn.academy.blocks.block-node.container :as container]
            [cn.academy.blocks.block-node.gui :as gui]
            [mcmod.registry :as registry]))

(defn register! [mod-id]
  ;; Register blocks
  (registry/register-block! mod-id "node_basic" (core/create-basic-node))
  (registry/register-block! mod-id "node_standard" (core/create-standard-node))
  (registry/register-block! mod-id "node_advanced" (core/create-advanced-node))
  
  ;; Register tile entities
  (registry/register-tile-entity! mod-id "node_basic" tile/tile-factory-createBasic)
  (registry/register-tile-entity! mod-id "node_standard" tile/tile-factory-createStandard)
  (registry/register-tile-entity! mod-id "node_advanced" tile/tile-factory-createAdvanced)

  ;; Register containers and GUIs
  (registry/register-container! mod-id "node" container/create-container)
  (registry/register-gui! mod-id "node" gui/create-gui))