(ns cn.academy.block.multiblock.example.controller-block
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.block.multiblock.interaction :as interaction]
            [cn.academy.block.multiblock.example.processing-machine :as machine]
            [cn.academy.api.block :as block-api])
  (:import [net.minecraft.block Block BlockState]
           [net.minecraft.util math.BlockPos Direction]
           [net.minecraft.world World]
           [net.minecraft.entity.player PlayerEntity]))

(defrecord ControllerBlock []
  block-api/IBlock
  (on-placed [_ world pos state player hand]
    (when-let [tile (block-api/get-tile-entity world pos)]
      (block-api/mark-dirty! tile)))
  
  (on-broken [_ world pos state]
    (interaction/handle-block-broken world pos))
  
  (on-activated [_ world pos state player hand]
    (interaction/handle-block-activation world pos player hand))
  
  (has-tile-entity? [_] true))

(defrecord ControllerTile [machine renderer]
  base/IMultiblockMember
  (get-controller [_]
    machine)
  
  (set-controller [_ _]
    nil)
  
  (can-connect? [_ other]
    (let [type (base/get-member-type other)]
      (contains? #{"casing" "energy_port"} type)))
  
  (get-member-type [_]
    "controller")

  block-api/ITileEntityRenderer
  (render [_ matrix-stack buffer ticks x y z]
    (when (render/should-render? renderer)
      (render/render-structure renderer matrix-stack buffer))))