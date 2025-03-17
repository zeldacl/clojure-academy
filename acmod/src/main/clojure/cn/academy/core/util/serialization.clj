(ns cn.academy.core.util.serialization
  (:require [cn.academy.core.util.logging :refer [log-error]]
            [clojure.edn :as edn])
  (:import [net.minecraft.nbt NBTTagCompound]))

(defprotocol INBTSerializable
  (write-to-nbt [this nbt])
  (read-from-nbt [this nbt]))

(defn write-edn-to-nbt [data ^NBTTagCompound nbt key]
  (.setString nbt key (pr-str data)))

(defn read-edn-from-nbt [^NBTTagCompound nbt key]
  (try
    (when-let [data (.getString nbt key)]
      (edn/read-string data))
    (catch Exception e
      (log-error e "Failed to read EDN from NBT for key" key)
      nil)))

(defn write-pos-to-nbt [pos ^NBTTagCompound nbt]
  (doto nbt
    (.setInteger "x" (.getX pos))
    (.setInteger "y" (.getY pos))
    (.setInteger "z" (.getZ pos))))

(defn read-pos-from-nbt [^NBTTagCompound nbt]
  (when (and (.hasKey nbt "x")
             (.hasKey nbt "y")
             (.hasKey nbt "z"))
    (net.minecraft.util.math.BlockPos.
      (.getInteger nbt "x")
      (.getInteger nbt "y")
      (.getInteger nbt "z"))))

(defn serialize-energy-data [energy-storage]
  {:stored (.getEnergyStored energy-storage)
   :capacity (.getMaxEnergyStored energy-storage)})

(defn deserialize-energy-data [data max-transfer]
  (let [stored (:stored data 0)
        capacity (:capacity data 0)]
    (reify net.minecraftforge.energy.IEnergyStorage
      (receiveEnergy [_ maxReceive simulate]
        (let [energy-received (min maxReceive 
                                 (- capacity stored)
                                 max-transfer)]
          (when-not simulate
            (set! stored (+ stored energy-received)))
          energy-received))
      
      (extractEnergy [_ maxExtract simulate]
        (let [energy-extracted (min maxExtract stored max-transfer)]
          (when-not simulate
            (set! stored (- stored energy-extracted)))
          energy-extracted))
      
      (getEnergyStored [_] stored)
      (getMaxEnergyStored [_] capacity)
      (canExtract [_] true)
      (canReceive [_] true))))