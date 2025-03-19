(ns cn.academy.registry
  (:require [clojure.tools.logging :as log]
            [cn.academy.core :as core]
            [mcmod.protocols :refer :all]
            [mcmod.registry :as mcr]
            [mcmod.util :as util]
            [cn.academy.blocks.wireless-matrix :refer [->WirelessMatrix]]
            [cn.academy.tile-entities.wireless-matrix-te :refer [->WirelessMatrixTE]]))
  (:import [net.minecraft.block Block]
           [net.minecraft.item Item]))

(def MOD-ID "cljacademy")

;; Registry state atoms
(def blocks (atom {}))
(def items (atom {}))
(def tile-entities (atom {}))

;; Protocol for version-specific implementations
(defprotocol RegistryProvider
  (register-block [this block-id block-constructor])
  (register-item [this item-id item-constructor])
  (register-tile-entity [this te-id te-class]))

;; Default properties for blocks/items
(defn default-block-properties []
  {:material net.minecraft.block.material.Material/ROCK
   :hardness 3.0
   :resistance 5.0
   :creative-tab core/creative-tab})

(defn default-item-properties []
  {:max-stack-size 64
   :creative-tab core/creative-tab})

;; Registration functions that will be called by version-specific code
(defn register-block! [provider block-id constructor]
  (swap! blocks assoc block-id constructor)
  (register-block provider block-id constructor))

(defn register-item! [provider item-id constructor]
  (swap! items assoc item-id constructor)
  (register-item provider item-id constructor))

(defn register-tile-entity! [provider te-id te-class]
  (swap! tile-entities assoc te-id te-class)
  (register-tile-entity provider te-id te-class))

;; Function to initialize all registrations for a specific version
(defn init-registrations [provider]
  (log/info "Initializing AcademyCraft registrations")
  (doseq [[block-id constructor] @blocks]
    (register-block provider block-id constructor))
  (doseq [[item-id constructor] @items]
    (register-item provider item-id constructor))
  (doseq [[te-id te-class] @tile-entities]
    (register-tile-entity provider te-id te-class)))

(defn register-blocks [registry]
  (util/register-mod-block registry 
                          MOD-ID
                          "wireless_matrix"
                          (->WirelessMatrix)))

(defn register-tile-entities [registry]
  (util/register-mod-tile-entity registry
                                MOD-ID
                                "wireless_matrix"
                                (->WirelessMatrixTE)))

(defn register-all []
  (let [registry (mcr/create-registry)]
    (register-blocks registry)
    (register-tile-entities registry)
    registry))