(ns cn.academy.core.util.serialization
  (:require [cn.academy.core.util.logging :refer [log-error]]
            [clojure.edn :as edn]
            [mcmod.nbt :as nbt]
            [mcmod.position :as position]
            [mcmod.protocols :as protocols]))

(defprotocol INBTSerializable
  (write-to-nbt [this nbt])
  (read-from-nbt [this nbt]))

(defn write-edn-to-nbt [data nbt key]
  (nbt/put-string nbt key (pr-str data)))

(defn read-edn-from-nbt [nbt key]
  (try
    (when-let [data (nbt/get-string nbt key)]
      (edn/read-string data))
    (catch Exception e
      (log-error e "Failed to read EDN from NBT for key" key)
      nil)))

(defn write-pos-to-nbt [pos nbt]
  (doto nbt
    (nbt/put-int "x" (:x pos))
    (nbt/put-int "y" (:y pos))
    (nbt/put-int "z" (:z pos))))

(defn read-pos-from-nbt [nbt]
  (when (and (nbt/has-key? nbt "x")
             (nbt/has-key? nbt "y")
             (nbt/has-key? nbt "z"))
    (position/create-position
      (nbt/get-int nbt "x")
      (nbt/get-int nbt "y")
      (nbt/get-int nbt "z"))))

(defn serialize-energy-data [energy-storage]
  {:stored (protocols/get-energy-stored energy-storage)
   :capacity (protocols/get-max-energy-stored energy-storage)})

(defn deserialize-energy-data [data max-transfer]
  (let [stored (atom (:stored data 0))
        capacity (:capacity data 0)]
    (reify protocols/IEnergyStorage
      (receive-energy [_ max-receive simulate]
        (let [energy-received (min max-receive 
                                 (- capacity @stored)
                                 max-transfer)]
          (when-not simulate
            (swap! stored + energy-received))
          energy-received))
      
      (extract-energy [_ max-extract simulate]
        (let [energy-extracted (min max-extract @stored max-transfer)]
          (when-not simulate
            (swap! stored - energy-extracted))
          energy-extracted))
      
      (get-energy-stored [_] @stored)
      (get-max-energy-stored [_] capacity)
      (can-extract? [_] true)
      (can-receive? [_] true))))