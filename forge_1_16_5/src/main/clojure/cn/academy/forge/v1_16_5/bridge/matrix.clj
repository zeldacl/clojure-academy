(ns cn.academy.forge.v1_16_5.bridge.matrix
  (:require [cn.academy.block.matrix.core :as matrix]
            [cn.academy.forge.v1_16_5.bridge.capability :as cap]
            [cn.academy.forge.v1_16_5.bridge.nbt :as nbt]
            [cn.academy.forge.v1_16_5.bridge.energy :as energy-bridge]
            [cn.academy.forge.v1_16_5.bridge.inventory :as inv-bridge])
  (:import [net.minecraft.tileentity TileEntity]
           [net.minecraft.util Direction]
           [net.minecraft.block Block]
           [net.minecraft.item ItemStack]
           [net.minecraft.nbt CompoundNBT]
           [net.minecraftforge.energy IEnergyStorage CapabilityEnergy]
           [net.minecraftforge.items IItemHandler CapabilityItemHandler]
           [net.minecraftforge.common.capabilities Capability]
           [net.minecraftforge.common.util LazyOptional]))

(defprotocol IMatrixBridge
  "Bridge between matrix component and Forge"
  (create-matrix-block [this matrix-def]
    "Create a Forge block for the matrix")
  (create-matrix-tile [this matrix-def]
    "Create a Forge tile entity for the matrix")
  (register-matrix [this registry id matrix-def]
    "Register the matrix with the registry"))

(deftype ForgeMatrixTile [matrix-def capabilities]
  TileEntity
  (load [_ block-state tag]
    (let [data (nbt/read-nbt tag)]
      (matrix/load-matrix matrix-def data)))
  
  (save [_ tag]
    (nbt/write-nbt tag (matrix/save-matrix matrix-def)))
  
  (getCapability [_ cap side]
    (let [side-dir (if side (.get side) nil)
          capability-key [cap side-dir]]
      (if-let [capability (get @capabilities capability-key)]
        capability
        (cond
          (= cap CapabilityEnergy/ENERGY)
          (let [energy-storage (matrix/get-energy-storage matrix-def side-dir)
                adapter (energy-bridge/create-adapter energy-storage)
                lazy-opt (LazyOptional/of (fn [] adapter))]
            (swap! capabilities assoc capability-key lazy-opt)
            lazy-opt)
          
          (= cap CapabilityItemHandler/ITEM_HANDLER_CAPABILITY)
          (let [inventory (matrix/get-inventory matrix-def side-dir)
                adapter (inv-bridge/create-forge-adapter inventory)
                lazy-opt (LazyOptional/of (fn [] (:item-handler adapter)))]
            (swap! capabilities assoc capability-key lazy-opt)
            lazy-opt)
            
          :else LazyOptional/EMPTY))))
  
  (onLoad [_]
    (matrix/on-load matrix-def))
  
  (setRemoved [_]
    (matrix/on-unload matrix-def)
    (doseq [[_ lazy-opt] @capabilities]
      (.invalidate lazy-opt))
    (reset! capabilities {}))
  
  (markDirty [this]
    (matrix/mark-dirty matrix-def)
    (proxy-super markDirty)))

(deftype ForgeMatrixBlock [matrix-def]
  Block
  (onRemove [_ state1 state2 world pos flag]
    (matrix/on-removed matrix-def))
  
  (use [_ state world pos player hand hit-x hit-y hit-z]
    (when-let [result (matrix/on-activated matrix-def
                                        {:x (.getX pos) :y (.getY pos) :z (.getZ pos)}
                                        {:player-id (.getUUID player)
                                         :hand hand})]
      (case (:action result)
        :success true
        :fail false
        false)))
  
  (getCloneItemStack [_ world pos state]
    (matrix/get-item-stack matrix-def))
  
  (neighborChanged [_ state world pos block fromPos]
    (matrix/on-neighbor-changed matrix-def
                              {:x (.getX pos) :y (.getY pos) :z (.getZ pos)}
                              {:x (.getX fromPos) :y (.getY fromPos) :z (.getZ fromPos)})))

(deftype ForgeMatrixBridge []
  IMatrixBridge
  (create-matrix-block [_ matrix-def]
    (ForgeMatrixBlock. matrix-def))
  
  (create-matrix-tile [_ matrix-def]
    (ForgeMatrixTile. matrix-def (atom {})))
  
  (register-matrix [_ registry id matrix-def]
    (registry/register-block registry
                           id
                           (create-matrix-block matrix-def))
    (registry/register-tile-entity registry
                                 id
                                 (create-matrix-tile matrix-def))))

(defn create-bridge []
  (->ForgeMatrixBridge))

(defn initialize-matrix [registry matrix-id]
  (let [bridge (create-bridge)
        matrix-def (matrix/create-matrix-def)]
    (.register-matrix bridge registry matrix-id matrix-def)
    matrix-def))

;; Function to create and register a matrix component with all required adapters
(defn register-matrix-component [registry matrix-id props]
  (let [matrix-def (matrix/create-matrix-def props)
        bridge (create-bridge)]
    (.register-matrix bridge registry matrix-id matrix-def)
    matrix-def))