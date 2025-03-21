(ns cn.academy.block.serialization
  (:require [clojure.tools.logging :as log]
            [mcmod.protocols :refer [INBTConverter INBTStorage]]
            [mcmod.nbt :as nbt]))

;; Block state serialization
(defn serialize-state [state]
  (let [nbt (nbt/create-compound)]
    (doseq [[k v] @state]
      (nbt/write-value! nbt (name k) v))
    nbt))

(defn deserialize-state [state nbt]
  (doseq [key (nbt/get-keys nbt)]
    (when-let [value (nbt/read-value nbt key)]
      (swap! state assoc (keyword key) value))))

;; Machine serialization implementation  
(extend-type clojure.lang.IPersistentMap
  INBTConverter
  (to-nbt [block]
    (let [nbt (nbt/create-compound)]
      ;; Write common data
      (nbt/write-string! nbt "type" (name (:type block)))
      
      ;; Write state data if present
      (when-let [state (:state block)]
        (nbt/write-compound! nbt "state" (serialize-state state)))
      
      ;; Write config data if present
      (when-let [config (:config block)]
        (nbt/write-compound! nbt "config" (nbt/write-map config)))
      
      nbt))
  
  (from-nbt [block nbt]
    (when-let [state (:state block)]
      (when-let [state-nbt (nbt/get-compound nbt "state")]
        (deserialize-state state state-nbt)))
    block))

;; Multiblock serialization helpers
(defn serialize-multiblock [controller]
  (let [nbt (to-nbt controller)]
    (when-let [members (get-in controller [:state :members])]
      (nbt/write-list! nbt "members"
        (map (fn [member]
               (let [pos (mcmod.block/get-pos member)]
                 {:x (:x pos)
                  :y (:y pos)
                  :z (:z pos)}))
             members)))
    nbt))

(defn deserialize-multiblock [controller nbt world]
  (from-nbt controller nbt)
  (when-let [member-list (nbt/get-list nbt "members")]
    (let [members (map (fn [pos-data]
                        (mcmod.block/get-block-at world pos-data))
                      member-list)]
      (swap! (:state controller) assoc :members (set members))))
  controller)

;; Save/load helpers
(defn save-block! [block]
  (try
    (let [nbt (to-nbt block)]
      (mcmod.block/write-nbt! block nbt)
      true)
    (catch Exception e
      (log/error "Failed to save block:" (.getMessage e))
      false)))

(defn load-block! [block]
  (try
    (when-let [nbt (mcmod.block/read-nbt block)]
      (from-nbt block nbt)
      true)
    (catch Exception e
      (log/error "Failed to load block:" (.getMessage e))
      false)))