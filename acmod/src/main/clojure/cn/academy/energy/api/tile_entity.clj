(ns cn.academy.energy.api.tile-entity)

(defprotocol IEnergyTile
  (energy-stored [this])
  (max-energy [this])
  (receive-energy [this amount]))

(defrecord EnergyStorage [properties]
  IEnergyTile
  (energy-stored [_]
    (:current-energy properties))
  
  (max-energy [_]
    (:max-energy properties))
  
  (receive-energy [this amount]
    (let [current (energy-stored this)
          max (max-energy this)
          new-amount (min (+ current amount) max)]
      (- new-amount current))))