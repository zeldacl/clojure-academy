(ns cn.academy.block.serialization
  (:require [clojure.tools.logging :as log]))

;; NBT serialization protocols
(defprotocol INBTSerializable
  (write-to-nbt [this])
  (read-from-nbt [this nbt]))

;; Block state serialization
(defn serialize-state [state]
  (let [nbt (mcmod.nbt/create-compound)]
    (doseq [[k v] @state]
      (mcmod.nbt/write-value! nbt (name k) v))
    nbt))

(defn deserialize-state [state nbt]
  (doseq [key (mcmod.nbt/get-keys nbt)]
    (when-let [value (mcmod.nbt/read-value nbt key)]
      (swap! state assoc (keyword key) value))))

;; Machine serialization implementation
(extend-type clojure.lang.IPersistentMap
  INBTSerializable
  (write-to-nbt [block]
    (let [nbt (mcmod.nbt/create-compound)]
      ;; Write common data
      (mcmod.nbt/write-value! nbt "type" (name (:type block)))
      
      ;; Write state data if present
      (when-let [state (:state block)]
        (mcmod.nbt/write-compound! nbt "state" (serialize-state state)))
      
      ;; Write config data if present
      (when-let [config (:config block)]
        (mcmod.nbt/write-compound! nbt "config" 
          (mcmod.nbt/write-map config)))
      
      nbt))
  
  (read-from-nbt [block nbt]
    (when-let [state (:state block)]
      (when-let [state-nbt (mcmod.nbt/get-compound nbt "state")]
        (deserialize-state state state-nbt)))
    block))

;; Multiblock serialization helpers
(defn serialize-multiblock [controller]
  (let [nbt (write-to-nbt controller)]
    (when-let [members (get-in controller [:state :members])]
      (mcmod.nbt/write-list! nbt "members"
        (map (fn [member]
               (let [pos (mcmod.block/get-pos member)]
                 {:x (:x pos)
                  :y (:y pos)
                  :z (:z pos)}))
             members)))
    nbt))

(defn deserialize-multiblock [controller nbt world]
  (read-from-nbt controller nbt)
  (when-let [member-list (mcmod.nbt/get-list nbt "members")]
    (let [members (map (fn [pos-data]
                        (mcmod.block/get-block-at world pos-data))
                      member-list)]
      (swap! (:state controller) assoc :members (set members))))
  controller)

;; Save/load helpers
(defn save-block! [block]
  (try
    (let [nbt (write-to-nbt block)]
      (mcmod.block/write-nbt! block nbt)
      true)
    (catch Exception e
      (log/error "Failed to save block:" (.getMessage e))
      false)))

(defn load-block! [block]
  (try
    (when-let [nbt (mcmod.block/read-nbt block)]
      (read-from-nbt block nbt)
      true)
    (catch Exception e
      (log/error "Failed to load block:" (.getMessage e))
      false)))