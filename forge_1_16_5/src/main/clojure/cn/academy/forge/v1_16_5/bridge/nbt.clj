(ns cn.academy.forge.v1_16_5.bridge.nbt
  (:import [net.minecraft.nbt CompoundNBT Tag ListNBT StringTag IntTag FloatTag DoubleTag LongTag]))

(defprotocol INbtBridge
  "Bridge for NBT data operations between platform-independent code and Forge"
  (create-nbt [this] "Create a new NBT compound")
  (write-to-nbt [this data nbt] "Write data to NBT")
  (read-from-nbt [this nbt] "Read data from NBT"))

(defrecord ForgeNbtBridge []
  INbtBridge
  (create-nbt [_]
    (CompoundNBT.))
  
  (write-to-nbt [_ data nbt]
    (doseq [[k v] data]
      (cond
        (string? v) (.putString nbt (name k) v)
        (integer? v) (.putInt nbt (name k) v)
        (float? v) (.putFloat nbt (name k) v)
        (double? v) (.putDouble nbt (name k) v)
        (map? v) (let [compound (CompoundNBT.)]
                   (write-to-nbt _ v compound)
                   (.put nbt (name k) compound))
        (sequential? v) (let [list (ListNBT.)]
                          (doseq [item v]
                            (cond
                              (string? item) (.add list (StringTag/valueOf item))
                              (integer? item) (.add list (IntTag/valueOf item))
                              (float? item) (.add list (FloatTag/valueOf item))
                              (double? item) (.add list (DoubleTag/valueOf item))
                              (map? item) (let [compound (CompoundNBT.)]
                                            (write-to-nbt _ item compound)
                                            (.add list compound))))
                          (.put nbt (name k) list))
        :else (.putString nbt (name k) (str v))))
    nbt)
  
  (read-from-nbt [_ nbt]
    (reduce (fn [m k]
              (let [tag (.get nbt k)]
                (assoc m (keyword k)
                       (cond
                         (instance? CompoundNBT tag) (read-from-nbt _ tag)
                         (instance? ListNBT tag) (mapv (fn [t]
                                                        (if (instance? CompoundNBT t)
                                                          (read-from-nbt _ t)
                                                          (.getAsString t)))
                                                      (vec tag))
                         :else (.getAsString tag)))))
            {}
            (iterator-seq (.keySet nbt)))))

;; Public API

(defn create-bridge []
  (->ForgeNbtBridge))

(defn write-nbt [tag data]
  (write-to-nbt (create-bridge) data tag))

(defn read-nbt [tag]
  (read-from-nbt (create-bridge) tag))