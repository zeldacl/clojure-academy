(ns cn.academy.data.storage
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core]
            [clojure.tools.logging :as log]))

(defrecord DataStorage [data]
  IDataStorage
  (store-data [this key data]
    (swap! (:data this) assoc key data))
    
  (load-data [this key]
    (get @(:data this) key))
    
  (delete-data [this key]
    (swap! (:data this) dissoc key))
    
  (list-data [this]
    (keys @(:data this))))

(defrecord WorldDataStorage [data world-key]
  IWorldData
  (load-world-data [this]
    (let [saved-data (load-data this world-key)]
      (reset! data saved-data)
      this))
      
  (save-world-data [this]
    (store-data this world-key @data)
    this)
    
  (mark-world-dirty [this]
    (save-world-data this)))

(defn create-storage []
  (->DataStorage (atom {})))
  
(defn create-world-storage [key]
  (->WorldDataStorage (atom {}) key))

;; NBT serialization implementations
(extend-protocol INBTConverter
  clojure.lang.PersistentHashMap
  (to-nbt [this]
    (into {} (map (fn [[k v]] [k (to-nbt v)]) this)))
    
  (from-nbt [this nbt]
    (into {} (map (fn [[k v]] [k (from-nbt v)]) nbt)))
    
  clojure.lang.PersistentVector  
  (to-nbt [this]
    (mapv to-nbt this))
    
  (from-nbt [this nbt]
    (mapv from-nbt nbt))
    
  java.lang.Number
  (to-nbt [this] this)
  (from-nbt [this nbt] nbt)
  
  java.lang.String
  (to-nbt [this] this)
  (from-nbt [this nbt] nbt)
  
  java.lang.Boolean
  (to-nbt [this] this)
  (from-nbt [this nbt] nbt)
  
  nil
  (to-nbt [this] nil)
  (from-nbt [this nbt] nil))

(defn serialize [data]
  (to-nbt data))
  
(defn deserialize [nbt]
  (from-nbt nbt))