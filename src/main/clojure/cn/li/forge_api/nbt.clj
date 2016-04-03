(ns cn.li.forge-api.nbt
  (:import (net.minecraft.nbt NBTBase NBTTagCompound NBTTagByte NBTTagShort NBTTagInt NBTTagLong NBTTagFloat NBTTagDouble NBTTagByteArray NBTTagIntArray NBTTagString)
           (clojure.lang PersistentVector PersistentList PersistentArrayMap)
           (net.minecraft.item ItemStack)))

(defn make-tag-name [k v]
  (let [t (cond
            (vector? v) "V"
            (list? v) "L"
            (= (type v) PersistentArrayMap) "M"
            (= (type v) ItemStack) "IS")]
    (str t "#" k "#" t)))

(defn get-tag-name [k]
  (let [key-string (clojure.string/split k #"#")
        first-t (first key-string)
        last-t (last key-string)
        t (when (> (count key-string) 2)
            (cond
              (= first-t last-t "V") "V"
              (= first-t last-t "L") "L"
              (= first-t last-t "M") "M"
              (= first-t last-t "IS") "IS"))]
    (if t
      [(clojure.string/join "#" (butlast (rest key-string))) t]
      [k t])))

(declare map->nbt)

(defn save-list [nbt k v]
  (if (or (empty? v)
          (every? #(or (= (type v) Integer) (= (type v) Long)) v))
    (.setIntArray nbt k v)
    (.setTag nbt (make-tag-name k v) (map->nbt (zipmap (range (count v)) v) (NBTTagCompound.)))))

(defn save-item-stack [nbt k v]
  (let [t (NBTTagCompound.)]
    (.writeToNBT v t)
    (.setTag nbt (make-tag-name k v) t)))

(defn load-item-stack [nbt k]
  (ItemStack/loadItemStackFromNBT (.getCompoundTag nbt k)))

(defn add-tag [^NBTTagCompound nbt k v]
  (cond-> nbt
          (instance? NBTBase v) (.setTag k v)
          (instance? Byte v) (.setByte k v)
          (instance? Short v) (.setShort k v)
          (instance? Integer v) (.setInteger k v)
          (instance? Long v) (.setLong k v)
          (instance? Float v) (.setFloat k v)
          (instance? Double v) (.setDouble k v)
          (instance? String v) (.setString k v)
          (= (type v) (type (byte-array []))) (.setByteArray k v)
          (= (type v) (type (int-array []))) (.setIntArray k v)
          (= (type v) PersistentVector) (save-list k v)
          (= (type v) PersistentList) (save-list k v)
          (= (type v) PersistentArrayMap) (.setTag (make-tag-name k v) (map->nbt v (NBTTagCompound.)))
          (instance? ItemStack v) (save-item-stack k v)))

(declare nbt->map)
(defn get-tag [nbt k]
  (let [tag (.getTag nbt k)
        [real-key t] (get-tag-name k)
        v (if t
            (cond
              (= t "V") (let [m (nbt->map (.getCompoundTag nbt k))]
                          (mapv #(get m %1) (map (comp keyword str) (range (count m)))))
              (= t "L") (let [m (nbt->map (.getCompoundTag nbt k))]
                          (map #(get m %1) (map (comp keyword str) (range (count m)))))
              (= t "M") (nbt->map (.getCompoundTag nbt k))
              (= t "ID") (load-item-stack nbt k))
            (cond
              (instance? NBTTagByte tag) (.getByte nbt k)
              (instance? NBTTagShort tag) (.getShort nbt k)
              (instance? NBTTagInt tag) (.getInteger nbt k)
              (instance? NBTTagLong tag) (.getLong nbt k)
              (instance? NBTTagFloat tag) (.getFloat nbt k)
              (instance? NBTTagDouble tag) (.getDouble nbt k)
              (instance? NBTTagByteArray tag) (into [] (.getByteArray nbt k))
              (instance? NBTTagIntArray tag) (into [] (.getIntArray nbt k))
              (instance? NBTTagString tag) (.getString nbt k)
              (instance? NBTTagCompound tag) (.getCompoundTag nbt k)))]
    (if t
      [real-key v]
      [k v])))

(defn map->nbt [map-data nbt]
  (reduce #(add-tag %1 (name (key %2)) (val %2)) nbt map-data))

(defn nbt->map [nbt]
  (let [nbt-keys (.func_150296_c nbt)]
    (into {} (mapv #(get-tag nbt %1) nbt-keys))))

(defn atom->nbt! [atom-data nbt]
  (map->nbt @atom-data nbt))

(defn nbt->atom! [atom-data nbt]
  (reset! atom-data (nbt->map nbt)))
