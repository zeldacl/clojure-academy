(ns cn.li.bridge.matrix.bridge
  (:require [cn.li.bridge.matrix.api :as matrix]
            [cn.li.bridge.matrix.energy :as energy]
            [cn.li.bridge.matrix.inventory :as inventory]
            [cn.li.bridge.matrix.capability :as capability]
            [cn.li.bridge.matrix.sync :as sync])
  (:import [net.minecraft.block Block AbstractBlock$Properties]
           [net.minecraft.block.material Material]
           [net.minecraft.tileentity TileEntity]
           [net.minecraft.nbt CompoundNBT]
           [net.minecraftforge.common.capabilities Capability ICapabilityProvider]
           [net.minecraftforge.energy CapabilityEnergy IEnergyStorage]
           [net.minecraftforge.common.util LazyOptional]
           [net.minecraft.util Direction]))

(def ^:private conversion-rates
  {:rf 4   ; 1 IF = 4 RF
   :eu 1   ; 1 IF = 1 EU
   :fe 4}) ; 1 IF = 4 FE (Forge Energy)

(defprotocol IMatrixBridge
  "Bridge between matrix component and Forge"
  (create-matrix-block [this matrix]
    "Create a Forge block for the matrix")
  (create-matrix-tile [this matrix]
    "Create a Forge tile entity for the matrix")
  (register-matrix [this registry id matrix]
    "Register the matrix with Forge registry"))

(deftype ForgeMatrixTile [matrix capabilities]
  TileEntity
  (load [_ nbt]
    (matrix/deserialize-from-nbt matrix (nbt/from-nbt nbt)))
  
  (save [_ nbt]
    (nbt/to-nbt nbt (matrix/serialize-to-nbt matrix)))
  
  (getCapability [_ cap side]
    (let [side-dir (when side (.get side))
          capability-key [cap side-dir]]
      (if-let [capability (get @capabilities capability-key)]
        capability
        (cond
          (= cap CapabilityEnergy/ENERGY)
          (let [lazy-opt (LazyOptional/of 
                         (fn [] (reify IEnergyStorage
                                (receiveEnergy [_ amount simulate]
                                  (matrix/receive-energy matrix amount simulate))
                                (extractEnergy [_ amount simulate]
                                  (matrix/extract-energy matrix amount simulate))
                                (getEnergyStored [_]
                                  (matrix/get-energy-stored matrix))
                                (getMaxEnergyStored [_]
                                  (matrix/get-energy-capacity matrix))
                                (canExtract [_] true)
                                (canReceive [_] true)))))]
            (swap! capabilities assoc capability-key lazy-opt)
            lazy-opt)
          :else LazyOptional/EMPTY))))
  
  ICapabilityProvider
  (invalidateCaps [_]
    (doseq [[_ cap] @capabilities]
      (.invalidate cap))
    (reset! capabilities {})))

(deftype ForgeMatrixBlock [matrix]
  Block
  (use [_ state world pos player hand hit]
    (when-let [result (matrix/on-activated matrix pos {:player player :hand hand})]
      true))
  
  (onBlockPlacedBy [_ world pos state placer stack]
    (matrix/on-placed matrix pos {:placer placer :stack stack}))
  
  (onReplaced [_ old-state new-state world pos is-moving]
    (matrix/on-removed matrix pos)))

(defrecord ForgeMatrixBridge []
  IMatrixBridge
  (create-matrix-block [_ matrix]
    (ForgeMatrixBlock. matrix))
  
  (create-matrix-tile [_ matrix]
    (ForgeMatrixTile. matrix (atom {})))
  
  (register-matrix [_ registry id matrix]
    (let [block (create-matrix-block matrix)
          tile (create-matrix-tile matrix)]
      (.register registry id block)
      (.register registry (str id "_tile") tile))))

(defrecord MatrixBridge [matrix]
  ICapabilityProvider
  (getCapability [_ cap side]
    (case cap
      CapabilityEnergy/ENERGY 
      (LazyOptional.of #(energy/create-energy-storage matrix))
      
      LazyOptional/EMPTY)))

(defn convert-energy-from [amount energy-type]
  (/ amount (get conversion-rates energy-type 1)))

(defn convert-energy-to [amount energy-type]
  (* amount (get conversion-rates energy-type 1)))

(defn create-matrix-bridge [matrix]
  (->MatrixBridge matrix))

;; Registration helper
(defn register-matrix [registry id matrix]
  (let [bridge (create-matrix-bridge matrix)
        cap-provider (capability/create-capability-provider matrix)
        sync-handler (sync/create-sync-handler matrix)]
    
    ;; Register network handlers
    (sync/register-sync-handlers registry)
    
    ;; Register capabilities  
    (capability/register-capabilities registry)
    
    matrix))

(defn create-bridge []
  (->ForgeMatrixBridge))