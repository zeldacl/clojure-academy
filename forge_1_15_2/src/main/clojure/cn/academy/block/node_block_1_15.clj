(ns cn.academy.block.node-block-1-15
  (:require [cn.academy.block.block.block-node :as core]
            [cn.academy.api.block :as block-api])
  (:import [net.minecraft.state.properties BooleanProperty IntegerProperty]
           [net.minecraft.state StateContainer$Builder]
           [net.minecraft.block BlockState]
           [net.minecraft.util Direction Hand]
           [net.minecraft.util.math BlockPos BlockRayTraceResult]
           [net.minecraft.world World IBlockReader]
           [net.minecraft.entity.player PlayerEntity]))

;; Block state properties for 1.15.2
(def connected-prop (BooleanProperty/create "connected"))
(def energy-prop (IntegerProperty/create "energy" 0 4))

(defrecord NodeBlock1_15 [core-block]
  block-api/IForgeBlock1_15
  (fillStateContainer [_ builder]
    (.add builder (into-array [connected-prop energy-prop]))
    nil)
  
  (getActualState [_ state world pos]
    (if-let [tile (.getTileEntity world pos)]
      (let [node-state (.getNodeState tile)
            energy (get-in node-state [:energy])
            max-energy (get-in node-state [:properties :max-energy])
            pct (Math/min 4 (Math/round (* 4 (/ energy max-energy))))]
        (-> state
            (.setValue connected-prop (boolean (:active node-state)))
            (.setValue energy-prop (int pct))))
      state))
  
  (use [_ state world pos player hand hit-result]
    (when (not (.isShiftKeyDown player))
      (core/get-container core-block player world pos)
      true))
  
  (getStateForPlacement [_ context]
    (.defaultBlockState this))
  
  (createTileEntity [_ state]
    (core/create-tile-entity core-block (.getLevel state) 0)))

(defn create-node-block [type]
  (->NodeBlock1_15 (core/create-node-block type)))