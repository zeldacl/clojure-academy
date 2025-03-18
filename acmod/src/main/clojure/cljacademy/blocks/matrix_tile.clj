(ns cljacademy.blocks.matrix-tile
  (:require [cljacademy.api.tileentity :as te]
            [cljacademy.api.energy :as energy]))

(defrecord MatrixTileEntity []
  energy/IEnergyHandler
  (getMaxEnergy [this] 100000)
  (getCurrentEnergy [this] (get this :energy 0))
  (receiveEnergy [this amount simulate]
    (let [current (get this :energy 0)
          max-receive (min amount (- (.getMaxEnergy this) current))
          new-energy (if simulate current (+ current max-receive))]
      (when-not simulate
        (assoc! this :energy new-energy))
      max-receive))
  
  te/ITileEntityState
  (write-to-nbt [this tag]
    (doto tag
      (.setDouble "energy" (get this :energy 0))
      (.setUUID "placer" (get this :placer-id nil))))
  
  (read-from-nbt [this tag]
    (-> this
        (assoc :energy (.getDouble tag "energy"))
        (assoc :placer-id (when (.hasUUID tag "placer")
                           (.getUUID tag "placer"))))))

(defn create-matrix-tile []
  (map->MatrixTileEntity {}))