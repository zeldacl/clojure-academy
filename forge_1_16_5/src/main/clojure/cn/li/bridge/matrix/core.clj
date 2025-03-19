(ns cn.li.bridge.matrix.core
  (:require [cn.li.bridge.matrix.api :as api]
            [cn.li.bridge.matrix.energy :as energy]
            [cn.li.bridge.matrix.inventory :as inventory]
            [cn.li.bridge.nbt.api :as nbt]
            [cn.li.bridge.matrix.events :refer [->MatrixFormEvent ->MatrixDeformEvent]]
            [cn.li.bridge.matrix.network-events :refer [->MatrixNetworkJoinEvent ->MatrixNetworkLeaveEvent ->MatrixNetworkSyncEvent]]
            [cn.li.bridge.matrix.energy-events :refer [->MatrixEnergyReceiveEvent ->MatrixEnergyExtractEvent ->MatrixEnergyUpdateEvent]]
            [cn.li.bridge.matrix.event-registry :as registry]
            [clojure.tools.logging :as log])
  (:import [net.minecraft.nbt CompoundNBT ListNBT]
           [net.minecraft.item ItemStack]
           [net.minecraft.util.math BlockPos]
           [net.minecraftforge.common MinecraftForge]))

(def matrix-structure
  [[-1 0 -1] [0 0 -1] [1 0 -1]
   [-1 0 0]  [0 0 0]  [1 0 0]
   [-1 0 1]  [0 0 1]  [1 0 1]])

(defrecord Matrix [state-atom network-atom inventory-handler position]
  api/IMatrix
  (get-energy-stored [_]
    (get-in @state-atom [:energy :stored] 0))
  
  (get-energy-capacity [this]
    (let [core-level (api/get-core-level this)
          plate-count (api/get-plate-count this)]
      (energy/calculate-capacity core-level plate-count)))
  
  (receive-energy [this amount simulate]
    (let [event (->MatrixEnergyReceiveEvent this amount simulate)]
      (when (not (.isCanceled (.post MinecraftForge/EVENT_BUS event)))
        (if simulate
          amount
          (do
            (swap! state-atom update-in [:energy :stored] + amount)
            amount)))))
  
  (extract-energy [this amount simulate]
    (let [event (->MatrixEnergyExtractEvent this amount simulate)]
      (when (not (.isCanceled (.post MinecraftForge/EVENT_BUS event)))
        (let [stored (get-in @state-atom [:energy :stored] 0)
              extract-amount (min amount stored)]
          (if simulate
            extract-amount
            (do
              (swap! state-atom update-in [:energy :stored] - extract-amount)
              extract-amount))))))
  
  (get-structure [_]
    (:structure @state-atom))
  
  (is-formed? [_]
    (:formed? @state-atom))
  
  (get-core-level [_]
    (let [core-item (get-in @state-atom [:inventory :core])]
      (if (.isEmpty core-item)
        0
        (.getCount core-item))))
  
  (get-plate-count [_]
    (count (get-in @state-atom [:inventory :plates] [])))
  
  (get-position [_]
    position)

  api/IMatrixNetwork
  (connect-to-network [_ network-id password]
    (let [event (->MatrixNetworkJoinEvent this network-id password)]
      (when (not (.isCanceled (.post MinecraftForge/EVENT_BUS event)))
        (reset! network-atom {:id network-id :password password})
        true)))
  
  (leave-network [_]
    (when-let [network-id (:id @network-atom)]
      (let [event (->MatrixNetworkLeaveEvent this network-id)]
        (when (not (.isCanceled (.post MinecraftForge/EVENT_BUS event)))
          (reset! network-atom nil)
          true))))
  
  (get-network-id [_]
    (:id @network-atom))
  
  (sync-with-client [_]
    (let [event (->MatrixNetworkSyncEvent this @state-atom)]
      (.post MinecraftForge/EVENT_BUS event)
      (doto (CompoundNBT.)
        (.putBoolean "formed" (:formed? @state-atom))
        (.putInt "energy" (get-in @state-atom [:energy :stored] 0)))))
  
  (deserialize-from-nbt [_ nbt]
    (swap! state-atom assoc
           :formed? (.getBoolean nbt "formed")
           :energy {:stored (.getInt nbt "energy")}))
  
  (serialize-to-nbt [_]
    (doto (CompoundNBT.)
      (.putBoolean "formed" (:formed? @state-atom))
      (.putInt "energy" (get-in @state-atom [:energy :stored] 0))
      (.put "inventory" (inventory/serialize-inventory @inventory-handler))))
  
  (on-network-tick [this]
    (when (and (api/is-formed? this)
               (api/get-network-id this))
      (let [energy-needed (- (api/get-energy-capacity this)
                            (api/get-energy-stored this))]
        (when (pos? energy-needed)
          ; Try request energy from network here
          ))))

  api/IMatrixInventory
  (get-inventory [_] 
    @inventory-handler)
  
  (is-valid-item? [_ slot item]
    (case slot
      0 true  ; Core slot accepts any item
      (and (>= slot 1) 
           (<= slot inventory/PLATE_SLOTS)))) ; Plate slots have specific validation

  api/IMatrixStructure  
  (form-structure [this]
    (let [event (->MatrixFormEvent this)]
      (.post MinecraftForge/EVENT_BUS event)
      (swap! state-atom assoc :formed? true)
      true))
  
  (break-structure [this]
    (let [event (->MatrixDeformEvent this)]
      (.post MinecraftForge/EVENT_BUS event)
      (swap! state-atom assoc :formed? false)
      (api/leave-network this)
      true)))

(defn create-matrix [position]
  (let [state-atom (atom {:formed? false
                         :energy {:stored 0}
                         :inventory {:core ItemStack/EMPTY
                                   :plates []}
                         :position nil})
        network-atom (atom nil)
        inventory-handler (atom (inventory/create-inventory-handler))]
    (let [matrix (->Matrix state-atom network-atom inventory-handler position)]
      (registry/register-matrix (registry/get-registry) matrix)
      matrix)))

(defn dispose-matrix [matrix]
  (registry/unregister-matrix (registry/get-registry) matrix))