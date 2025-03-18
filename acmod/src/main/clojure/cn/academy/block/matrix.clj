(ns cn.academy.block.matrix
  (:require [cn.academy.block.matrix-state :as state]
            [cn.academy.block.matrix-inventory :as inv]
            [cn.academy.block.matrix-energy :as energy]
            [cn.academy.block.matrix-config :as config]))

(defprotocol IMatrix
  "Main protocol for matrix functionality"
  (get-state [this]
    "Get matrix state")
  (get-inventory [this]
    "Get matrix inventory")
  (get-energy-storage [this]
    "Get energy storage")
  (get-config [this]
    "Get matrix configuration")
  (update! [this]
    "Update matrix logic")
  (is-valid-core? [this item]
    "Check if item can be used as core")
  (is-valid-plate? [this item]
    "Check if item can be used as plate"))

(defrecord Matrix [state inventory energy config]
  IMatrix
  (get-state [_] state)
  
  (get-inventory [_] inventory)
  
  (get-energy-storage [_] energy)
  
  (get-config [_] config)
  
  (update! [_]
    (when (state/is-formed? state)
      (energy/update-energy! energy)))
  
  (is-valid-core? [_ item]
    (and item
         (= (:type item) :matrix_core)
         (pos? (:level item))))
  
  (is-valid-plate? [_ item]
    (and item
         (= (:type item) :matrix_plate))))

(def matrix-structure
  "Define the 3x3x3 matrix structure"
  (for [x [-1 0 1]
        y [-1 0 1]
        z [-1 0 1]]
    [x y z]))

(defn create-matrix []
  (let [config (config/create-config)
        inventory (inv/create-matrix-inventory #(is-valid-core? % nil)
                                             #(is-valid-plate? % nil))
        state (state/create-matrix-state inventory)
        energy (energy/create-matrix-energy state config)]
    (->Matrix state inventory energy config)))

(defn get-core-level [matrix]
  (state/get-core-level (:state matrix)))

(defn get-plate-count [matrix]
  (state/get-plate-count (:state matrix)))

(defn get-energy-stored [matrix]
  (energy/get-energy-stored (:energy matrix)))

(defn get-energy-capacity [matrix]
  (energy/get-energy-capacity (:energy matrix)))

(defn get-position [matrix]
  (state/get-position (:state matrix)))

(defn set-position! [matrix pos]
  (state/set-position! (:state matrix) pos))

(defn is-formed? [matrix]
  (state/is-formed? (:state matrix)))

(defn try-form! [matrix validator]
  (state/try-form! (:state matrix) validator))

(defn break! [matrix]
  (state/break! (:state matrix)))