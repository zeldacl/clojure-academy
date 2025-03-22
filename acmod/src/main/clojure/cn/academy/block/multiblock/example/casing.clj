(ns cn.academy.block.multiblock.example.casing
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.block.multiblock.multiblock-member :as member]
            [cn.academy.protocols.block :as block-api]
            [clojure.tools.logging :as log]))

(defrecord CasingBlock []
  block-api/IBlock
  (on-placed [_ world pos state player hand]
    (when-let [tile (block-api/get-tile-entity world pos)]
      (block-api/mark-dirty! tile)))
  
  (on-broken [_ world pos state]
    (interaction/handle-block-broken world pos))
  
  (has-tile-entity? [_] true))

(defrecord CasingTile [state-atom]
  base/IMultiblockMember
  (get-controller [_]
    (:controller @state-atom))
  
  (set-controller [this controller]
    (swap! state-atom assoc :controller controller)
    (block-api/mark-dirty! this))
  
  (can-connect? [_ other]
    (contains? #{"controller" "energy_port"}
              (base/get-member-type other)))
  
  (get-member-type [_]
    "casing")
  
  block-api/ITileEntity
  (load-data [_ data]
    (reset! state-atom {:controller (:controller data)}))
  
  (save-data [_]
    @state-atom))

(defn create-casing []
  (->CasingTile (atom {:controller nil})))