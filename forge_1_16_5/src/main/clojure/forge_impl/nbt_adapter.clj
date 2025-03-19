(ns forge-impl.nbt-adapter
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraft.nbt CompoundNBT ListNBT INBT]
           [net.minecraft.util ResourceLocation]))

(defprotocol INBTConversion
  (to-nbt [this] "Convert value to NBT")
  (from-nbt [this nbt] "Convert NBT back to value"))

(extend-protocol INBTConversion
  clojure.lang.PersistentArrayMap
  (to-nbt [this]
    (let [nbt (CompoundNBT.)]
      (doseq [[k v] this]
        (.put nbt (name k) (to-nbt v)))
      nbt))
  (from-nbt [_ nbt]
    (into {} (for [key (.keySet nbt)]
               [(keyword key) (from-nbt nil (.get nbt key))])))

  clojure.lang.PersistentVector
  (to-nbt [this]
    (let [list (ListNBT.)]
      (doseq [item this]
        (.add list (to-nbt item)))
      list))
  (from-nbt [_ nbt]
    (mapv #(from-nbt nil %) nbt))

  Number
  (to-nbt [this]
    (.valueOf this))
  (from-nbt [_ nbt]
    nbt)

  String
  (to-nbt [this]
    this)
  (from-nbt [_ nbt]
    nbt)

  Boolean
  (to-nbt [this]
    (byte (if this 1 0)))
  (from-nbt [_ nbt]
    (= nbt (byte 1)))

  nil
  (to-nbt [_]
    (CompoundNBT.))
  (from-nbt [_ nbt]
    nil))

(defn write-nbt! [compound key value]
  (let [nbt-value (to-nbt value)]
    (.put compound (name key) nbt-value)))

(defn read-nbt [compound key default]
  (if (.contains compound (name key))
    (from-nbt nil (.get compound (name key)))
    default))