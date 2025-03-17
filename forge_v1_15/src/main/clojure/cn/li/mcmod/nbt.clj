(ns cn.li.mcmod.nbt
  (:import (net.minecraft.nbt CompoundNBT INBT ByteNBT ShortNBT IntNBT LongNBT FloatNBT DoubleNBT ByteArrayNBT IntArrayNBT StringNBT LongArrayNBT)
           (net.minecraftforge.common.util INBTSerializable LazyOptional)))

(defn get-from-tag
  [^CompoundNBT nbt k default-v]
  (let [v (.get nbt k)
        value (cond
                (instance? ByteNBT v) (.getByte nbt k)
                (instance? ShortNBT v) (.getShort nbt k)
                (instance? IntNBT v) (.getInt nbt k)
                (instance? LongNBT v) (.getLong nbt k)
                (instance? FloatNBT v) (.getFloat nbt k)
                (instance? DoubleNBT v) (.getDouble nbt k)
                (instance? StringNBT v) (.getString nbt k)
                (instance? ByteArrayNBT v) (into [] (.getByteArray nbt k))
                (instance? IntArrayNBT v) (into [] (.getIntArray nbt k))
                (instance? LongArrayNBT v) (into [] (.getLongArray nbt k))
                (instance? INBTSerializable default-v) (let [vv (.getCompound nbt k)]
                                                         (.deserializeNBT ^INBTSerializable default-v vv)
                                                         default-v)
                :else (throw (RuntimeException. (str k v))))]
    value))

(defn add-to-tag
  [k v ^CompoundNBT nbt]
  (cond (instance? INBT v) (.put nbt k v)
        (instance? INBTSerializable v) (.put nbt k (.serializeNBT ^INBTSerializable v))
        (instance? java.util.List v) (cond (empty? v) (.putLongArray nbt ^String k ^java.util.List v)
                                           (= (type (first v)) java.lang.Integer) (.putIntArray nbt ^String k ^java.util.List v)
                                           (= (type (first v)) java.lang.Long) (.putLongArray nbt ^String k ^java.util.List v)
                                           :else (throw (RuntimeException. ^String (str v))))
        :else (cond
                (= (type v) java.lang.Byte) (.putByte nbt k v)
                (= (type v) java.lang.Short) (.putShort nbt k v)
                (= (type v) java.lang.Integer) (.putInt nbt k v)
                (= (type v) java.lang.Long) (.putLong nbt k v)
                (= (type v) java.util.UUID) (.putUUID nbt k v)
                (= (type v) java.lang.Float) (.putFloat nbt k v)
                (= (type v) java.lang.Double) (.putDouble nbt k v)
                (= (type v) java.lang.String) (.putString nbt k v)
                (= (type v) "[B") (.putByteArray nbt k v)
                (= (type v) "[I") (.putIntArray nbt ^String k #^ints v)
                (= (type v) "[J") (.putLongArray nbt ^String k #^longs v)
                (= (type v) java.lang.Boolean) (.putBoolean nbt k v)
                :else (throw (RuntimeException. ^String (str v)))))
  nbt)

(defn map->nbt
  [nbt-map ^CompoundNBT nbt]
  (reduce #(let [k (key %2)
                 v (val %2)]
             (if (instance? LazyOptional v)
               (do
                 (.ifPresent ^LazyOptional v (fn [h] (add-to-tag (name k) h %1)))
                 %1)
               (add-to-tag (name k) v %1))) nbt nbt-map))

(defn read-tag-data!
  [entity-atom ^CompoundNBT nbt]
  (let [nbt-map (deref entity-atom)
        data (reduce (fn [m [k v]]
                       (if (instance? LazyOptional v)
                         (do
                           (.ifPresent ^LazyOptional v (fn [h] (get-from-tag nbt (name k) h)))
                           m)
                         (assoc m k (get-from-tag nbt (name k) v)))) {} nbt-map)]
    (reset! entity-atom data)))

(defn write-tag-data!
  [entity-atom ^CompoundNBT nbt]
  (let [nbt-map (deref entity-atom)]
    (map->nbt nbt-map nbt)))
