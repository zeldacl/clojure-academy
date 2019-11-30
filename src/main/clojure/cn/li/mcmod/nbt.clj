(ns cn.li.mcmod.nbt
  (:import (net.minecraft.nbt CompoundNBT INBT ByteNBT ShortNBT IntNBT LongNBT FloatNBT DoubleNBT ByteArrayNBT IntArrayNBT StringNBT LongArrayNBT)
           (net.minecraftforge.common.util INBTSerializable LazyOptional)))


(defn get-from-tag
  [^CompoundNBT nbt k default-v]
  (let [v (.get nbt k)
        value (condp instance? v
                ByteNBT (.getByte nbt k)
                ShortNBT (.getShort nbt k)
                IntNBT (.getInt nbt k)
                LongNBT (.getLong nbt k)
                FloatNBT (.getFloat nbt k)
                DoubleNBT (.getDouble nbt k)
                StringNBT (.getString nbt k)
                ByteArrayNBT (into [] (.getByteArray nbt k))
                IntArrayNBT (into [] (.getIntArray nbt k))
                LongArrayNBT (into [] (.getLongArray nbt k))
                ;StringNBT (string-tag-handler (.getString nbt k))
                ;CompoundNBT (.getCompound nbt k)
                (condp instance? default-v
                  INBTSerializable (let [vv (.getCompound nbt k)]
                                     (.deserializeNBT default-v vv)
                                     default-v)
                  (throw (RuntimeException. (str k v)))))]
    value))

;https://stackoverflow.com/questions/30509452/how-to-get-type-of-array-elements-in-clojure-from-fields-declared-in-java
(defn add-to-tag
  [k v ^CompoundNBT nbt]
  (cond (instance? INBT v) (.put nbt k v)
        (instance? INBTSerializable v) (.put nbt k (.serializeNBT ^INBTSerializable v))
        (instance? java.util.List v) (cond (empty? v) (.putLongArray nbt ^String k ^java.util.List v)
                                           (= (type (first v)) java.lang.Integer) (.putIntArray nbt ^String k ^java.util.List v)
                                           (= (type (first v)) java.lang.Long) (.putLongArray nbt ^String k ^java.util.List v)
                                           :else (throw (RuntimeException. ^String (str v))))
        :else (condp = (type v)
                java.lang.Byte (.putByte nbt k v)
                java.lang.Short (.putShort nbt k v)
                java.lang.Integer (.putInt nbt k v)
                java.lang.Long (.putLong nbt k v)
                java.util.UUID (.putUniqueId nbt k v)
                java.lang.Float (.putFloat nbt k v)
                java.lang.Double (.putDouble nbt k v)
                java.lang.String (.putString nbt k v)
                "[B" (.putByteArray nbt k v)
                "[I" (.putIntArray nbt ^String k #^ints v)
                "[J" (.putLongArray nbt ^String k #^longs v)
                java.lang.Boolean (.putBoolean nbt k v)
                ;java.util.List (.putLongArray nbt k ^java.util.List v)
                ;clojure.lang.PersistentVector (handle-colls k v nbt)
                ;clojure.lang.PersistentList (handle-colls k v nbt)
                ; clojure.lang.PersistentArrayMap (.setTag nbt (str k "§HASHMAP") (map->nbt v (NBTTagCompound.)))
                ;net.minecraft.item.ItemStack (save-istack k v nbt)
                ;(.setString nbt k (prn-str v))
                (throw (RuntimeException. ^String (str v)))))
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