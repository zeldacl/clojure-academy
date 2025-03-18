(ns cn.academy.forge-1-16.nbt-bridge
  (:require [cn.academy.block.matrix-serialization :as ser])
  (:import [net.minecraft.nbt CompoundNBT ListNBT]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.util ResourceLocation]))

(defprotocol INbtBridge
  "Bridge between platform-independent data and Forge NBT"
  (write-to-nbt [this data tag]
    "Write data to NBT tag")
  (read-from-nbt [this tag]
    "Read data from NBT tag")
  (create-nbt [this]
    "Create new NBT tag"))

(defrecord ForgeNbtBridge []
  INbtBridge
  (write-to-nbt [_ data tag]
    (when data
      ; Write state data
      (when-let [state (:state data)]
        (let [state-tag (CompoundNBT.)]
          (.putBoolean state-tag "formed" (:formed? state))
          (.putDouble state-tag "energy" (:energy state))
          (when-let [pos (:position state)]
            (.putInt state-tag "pos_x" (:x pos))
            (.putInt state-tag "pos_y" (:y pos))
            (.putInt state-tag "pos_z" (:z pos)))
          (.put tag "state" state-tag)))
      
      ; Write inventory data
      (when-let [inv (:inventory data)]
        (let [inv-tag (CompoundNBT.)]
          (when-let [core (:core inv)]
            (.put inv-tag "core" (write-item-stack core)))
          (let [plates-tag (ListNBT.)]
            (doseq [plate (:plates inv)]
              (.add plates-tag (write-item-stack plate)))
            (.put inv-tag "plates" plates-tag))
          (.put tag "inventory" inv-tag)))
      
      ; Write energy data
      (when-let [energy (:energy data)]
        (let [energy-tag (CompoundNBT.)]
          (.putDouble energy-tag "stored" (:stored energy))
          (let [nodes-tag (ListNBT.)]
            (doseq [node (:nodes energy)]
              (let [node-tag (CompoundNBT.)]
                (.putString node-tag "type" (name (:type node)))
                (when-let [pos (:pos node)]
                  (.putInt node-tag "pos_x" (:x pos))
                  (.putInt node-tag "pos_y" (:y pos))
                  (.putInt node-tag "pos_z" (:z pos)))
                (.add nodes-tag node-tag)))
            (.put energy-tag "nodes" nodes-tag))
          (.put tag "energy" energy-tag))))
    tag)
  
  (read-from-nbt [_ tag]
    (when tag
      (let [data {}]
        ; Read state data
        (when-let [state-tag (.getCompound tag "state")]
          (assoc data :state
                 {:formed? (.getBoolean state-tag "formed")
                  :energy (.getDouble state-tag "energy")
                  :position (when (.contains state-tag "pos_x")
                            {:x (.getInt state-tag "pos_x")
                             :y (.getInt state-tag "pos_y")
                             :z (.getInt state-tag "pos_z")})}))
        
        ; Read inventory data
        (when-let [inv-tag (.getCompound tag "inventory")]
          (assoc data :inventory
                 {:core (when (.contains inv-tag "core")
                         (read-item-stack (.getCompound inv-tag "core")))
                  :plates (vec (for [i (range (.size (.getList inv-tag "plates" 10)))]
                               (read-item-stack 
                                 (.getCompound (.getList inv-tag "plates" 10) i))))}))
        
        ; Read energy data
        (when-let [energy-tag (.getCompound tag "energy")]
          (assoc data :energy
                 {:stored (.getDouble energy-tag "stored")
                  :nodes (vec (for [i (range (.size (.getList energy-tag "nodes" 10)))]
                              (let [node-tag (.getCompound (.getList energy-tag "nodes" 10) i)]
                                {:type (keyword (.getString node-tag "type"))
                                 :pos (when (.contains node-tag "pos_x")
                                       {:x (.getInt node-tag "pos_x")
                                        :y (.getInt node-tag "pos_y")
                                        :z (.getInt node-tag "pos_z")})})))}))
        data)))
  
  (create-nbt [_]
    (CompoundNBT.)))

(defn- write-item-stack [item]
  (let [tag (CompoundNBT.)]
    (when item
      (.putString tag "item" (str (:item item)))
      (.putInt tag "count" (:count item)))
    tag))

(defn- read-item-stack [tag]
  (when (and tag (.contains tag "item"))
    {:item (ResourceLocation. (.getString tag "item"))
     :count (.getInt tag "count")}))

(defn create-nbt-bridge []
  (->ForgeNbtBridge))