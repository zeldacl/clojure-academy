(ns cn.academy.forge-1-16.nbt-bridge
  (:import [net.minecraft.nbt CompoundNBT ListNBT]
           [net.minecraft.util IntReferenceHolder]))

;; Bridge for working with Minecraft's NBT data system
(defrecord ForgeNbtBridge []
  Object
  (create-nbt [_]
    (CompoundNBT.))
  
  (create-list-nbt [_]
    (ListNBT.))
  
  (write-to-nbt [this data tag]
    (cond
      (map? data)
      (doseq [[k v] data]
        (.write-value this tag (name k) v))
      
      (seq? data)
      (let [list (ListNBT.)]
        (doseq [item data]
          (.add list (.to-nbt this item)))
        (.put tag "list" list)))
    tag)
  
  (read-from-nbt [this tag]
    (let [result (atom {})]
      (doseq [key (.getAllKeys tag)]
        (swap! result assoc (keyword key) (.read-value this tag key)))
      @result))
  
  (write-value [this tag key value]
    (cond
      (string? value)
      (.putString tag key value)
      
      (number? value)
      (if (integer? value)
        (.putInt tag key value)
        (.putDouble tag key value))
      
      (boolean? value)
      (.putBoolean tag key value)
      
      (map? value)
      (let [compound (CompoundNBT.)]
        (.write-to-nbt this value compound)
        (.put tag key compound))
      
      (coll? value)
      (let [list (ListNBT.)]
        (doseq [item value]
          (.add list (.to-nbt this item)))
        (.put tag key list))))
  
  (read-value [this tag key]
    (cond
      (.contains tag key (byte 8)) ;; String
      (.getString tag key)
      
      (.contains tag key (byte 3)) ;; Int
      (.getInt tag key)
      
      (.contains tag key (byte 6)) ;; Double
      (.getDouble tag key)
      
      (.contains tag key (byte 1)) ;; Boolean
      (.getBoolean tag key)
      
      (.contains tag key (byte 10)) ;; Compound
      (.read-from-nbt this (.getCompound tag key))
      
      (.contains tag key (byte 9)) ;; List
      (let [list (.getList tag key (byte 10))]
        (mapv #(.read-from-nbt this %) list))))
  
  (to-nbt [this value]
    (cond
      (string? value)
      (doto (CompoundNBT.) (.putString "v" value))
      
      (number? value)
      (if (integer? value)
        (doto (CompoundNBT.) (.putInt "v" value))
        (doto (CompoundNBT.) (.putDouble "v" value)))
      
      (boolean? value)
      (doto (CompoundNBT.) (.putBoolean "v" value))
      
      (map? value)
      (let [compound (CompoundNBT.)]
        (.write-to-nbt this value compound)
        compound))))

(defn create-nbt-bridge []
  (->ForgeNbtBridge))