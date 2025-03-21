(ns cn.academy.block.config
  (:require [mcmod.protocols.block :as block-protocol]
            [clojure.tools.logging :as log]))

(def base-block-properties
  {:metal {:hardness 4.0
           :resistance 8.0
           :has-tile-entity true
           :harvest-level 1}
   :machine {:hardness 3.5 
             :resistance 17.5
             :has-tile-entity true}
   :ore {:hardness 3.0
         :resistance 5.0
         :harvest-level 2}})

(def block-config
  {"wireless_matrix" (merge (:metal base-block-properties)
                           {:light-level 7})
   "energy_generator" (merge (:machine base-block-properties)
                           {:light-level 7})
   "cat_engine" (merge (:metal base-block-properties)
                      {:light-level 0})
   "energy_storage" (merge (:machine base-block-properties)
                         {:light-level 0})
   "crystal_ore" (:ore base-block-properties)})

(defn get-block-properties [block-id]
  (get block-config block-id))

;; Configuration state management
(defn create-config [initial-values]
  (atom initial-values))

;; NBT serialization/deserialization
(defn write-config-to-nbt [config]
  (let [nbt (mcmod.nbt/create-compound)]
    (doseq [[k v] @config]
      (mcmod.nbt/write-value! nbt (name k) v))
    nbt))

(defn read-config-from-nbt [config nbt]
  (doseq [key (mcmod.nbt/get-keys nbt)]
    (when-let [value (mcmod.nbt/read-value nbt key)]
      (swap! config assoc (keyword key) value))))

;; Configuration validation
(defn validate-config! [config validators]
  (try
    (doseq [[k validator] validators]
      (when-let [value (get @config k)]
        (validator value)))
    true
    (catch Exception e
      (log/error "Configuration validation failed:" (.getMessage e))
      false)))

;; Default validators
(def default-validators
  {:energy-capacity #(and (number? %) (>= % 0))
   :max-output #(and (number? %) (>= % 0))
   :base-efficiency #(and (number? %) (<= 0 % 1))
   :work-speed #(and (number? %) (> % 0))
   :inventory-size #(and (integer? %) (> % 0))})

;; Configuration update helpers
(defn update-config! [config key value]
  (swap! config assoc key value))

(defn merge-config! [config updates]
  (swap! config merge updates))

;; Config persistence helpers
(defprotocol IConfigurable
  (save-config! [this])
  (load-config! [this])
  (reset-config! [this]))

(defn make-configurable [block config validators]
  (reify IConfigurable
    (save-config! [_]
      (when (validate-config! config validators)
        (let [nbt (write-config-to-nbt config)]
          (mcmod.block/write-nbt! block nbt))))
    
    (load-config! [_]
      (when-let [nbt (mcmod.block/read-nbt block)]
        (read-config-from-nbt config nbt)))
    
    (reset-config! [_]
      (reset! config (get-in block [:config :default-config]))
      (save-config! _))))