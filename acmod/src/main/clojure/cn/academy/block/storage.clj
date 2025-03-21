(ns cn.academy.block.storage
  (:require [mcmod.protocols :refer [IDataStorage IWorldData INBTStorage]]
            [clojure.tools.logging :as log]))

;; Storage state tracking
(def storage-state
  (atom {:world-data {}
         :block-data {}}))

;; World data implementation
(defrecord WorldDataStorage [state-atom world-id]
  IWorldData
  (load-world-data [_]
    (get-in @storage-state [:world-data world-id]))
  
  (save-world-data [this]
    (let [data @state-atom]
      (swap! storage-state assoc-in [:world-data world-id] data)))
  
  (mark-world-dirty [_]
    (swap! storage-state assoc-in [:world-data world-id :dirty] true)))

;; Block data implementation  
(defrecord BlockDataStorage [block-id state-atom]
  IDataStorage
  (store-data [_ key data]
    (swap! state-atom assoc key data)
    (swap! storage-state assoc-in [:block-data block-id key] data))
  
  (load-data [_ key]
    (get @state-atom key))
  
  (delete-data [_ key]
    (swap! state-atom dissoc key)
    (swap! storage-state update-in [:block-data block-id] dissoc key))
  
  (list-data [_]
    (keys @state-atom)))

;; NBT storage implementation
(defrecord NBTDataStorage [state-atom]
  INBTStorage
  (put-value [_ key value]
    (swap! state-atom assoc key value))
  
  (get-value [_ key]
    (get @state-atom key))
  
  (remove-value [_ key]
    (swap! state-atom dissoc key))
  
  (get-all-keys [_]
    (keys @state-atom)))

;; Storage factory functions
(defn create-world-storage [world-id]
  (->WorldDataStorage (atom {}) world-id))

(defn create-block-storage [block-id]
  (->BlockDataStorage block-id (atom {})))

(defn create-nbt-storage []
  (->NBTDataStorage (atom {})))

;; Data persistence helpers
(defn save-block-data! [block]
  (when-let [storage (:storage block)]
    (when (satisfies? IDataStorage storage)
      (.store-data storage :state @(:state block)))))

(defn load-block-data! [block data]
  (when-let [storage (:storage block)]
    (when (satisfies? IDataStorage storage)
      (reset! (:state block) data))))

;; Initialize storage system
(defn init-storage! []
  (reset! storage-state {:world-data {}
                        :block-data {}}))