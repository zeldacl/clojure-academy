(ns cn.academy.block.matrix
  (:require [cn.academy.block.matrix.config :as config]
            [cn.academy.block.matrix.state :as state]
            [cn.academy.block.matrix.network :as network]
            [cn.academy.block.matrix.energy :as energy]
            [cn.academy.block.matrix.inventory :as inventory]
            [cn.academy.block.matrix.container :as container]
            [cn.academy.block.matrix.render :as render]
            [cn.academy.block.matrix.utils :as utils]
            [cn.academy.block.multiblock :as multiblock]))

;; Define sub-blocks for multiblock structure
(def ^:private matrix-sub-blocks
  [[0 0 1] [1 0 1] [1 0 0] [0 1 0] [0 1 1] [1 1 1] [1 1 0]])

(defprotocol IMatrixMultiblock
  (is-complete? [this])
  (get-origin [this])
  (get-sub-blocks [this]))

(defprotocol IMatrix
  "Core matrix functionality"
  (get-position [this])
  (get-placer-name [this])
  (set-placer! [this name])
  (is-formed? [this])
  (is-valid? [this])
  (get-core-level [this])
  (get-plate-count [this])
  (get-energy-stored [this])
  (get-energy-capacity [this])
  (get-range [this])
  (get-bandwidth [this])
  (working? [this])
  (join-network [this network-id password])
  (leave-network [this])
  (can-interact? [this player])
  (handle-block-activated [this world pos state player]
    "Handle block activation (right-click)")
  (handle-block-placed [this world pos state placer stack]
    "Handle block placement"))

(defrecord Matrix [id pos state inventory network energy renderer]
  IMatrix
  (get-position [_] pos)
  
  (get-placer-name [_]
    (state/get-placer state))
  
  (set-placer! [_ name]
    (state/set-placer! state name))
  
  (is-formed? [_]
    (state/is-formed? state))
  
  (is-valid? [_]
    (state/is-valid? state))
  
  (get-core-level [_]
    (inventory/get-core-level inventory))
  
  (get-plate-count [_]
    (inventory/get-plate-count inventory))
  
  (get-energy-stored [_]
    (energy/get-energy-stored energy))
  
  (get-energy-capacity [_]
    (energy/get-energy-capacity energy))
  
  (get-range [_]
    (network/get-range network))
  
  (get-bandwidth [_]
    (network/get-bandwidth network))
  
  (working? [this]
    (and (is-formed? this)
         (pos? (get-energy-stored this))))
  
  (join-network [_ network-id password]
    (network/join network-id password))
  
  (leave-network [_]
    (network/leave))
  
  (can-interact? [this player]
    (container/can-interact-with? player))
  
  (handle-block-activated [_ world pos state player]
    (when-not (.isSneaking player)
      (when-let [origin (multiblock/get-origin world pos)]
        (container/open-gui player world origin)
        true)))
  
  (handle-block-placed [_ world pos state placer stack]
    (when (instance? net.minecraft.entity.player.EntityPlayer placer)
      (when-let [tile (get-tile-entity world pos)]
        (set-placer! this (.getName placer)))))
  
  IMatrixMultiblock
  (is-complete? [_]
    (multiblock/is-complete? pos matrix-sub-blocks))
  
  (get-origin [_]
    (multiblock/get-origin pos matrix-sub-blocks))
  
  (get-sub-blocks [_]
    matrix-sub-blocks))

(defn create-matrix
  "Create new matrix instance"
  [& {:keys [id position config]
      :or {id (str (random-uuid))
           position {:x 0 :y 0 :z 0}
           config (config/create-config)}}]
  (let [state (state/create-matrix-state config)
        inventory (inventory/create-inventory)
        network (network/create-network)
        energy (energy/create-energy state)
        renderer (render/create-renderer)]
    (->Matrix id
             position
             state
             inventory
             network 
             energy
             renderer)))