(ns cn.academy.api.public
  "Public API for ClojureAcademy mod integration"
  (:require [mcmod.capabilities :as cap]
            [mcmod.protocols :refer :all]
            [cn.academy.energy.transfer-handler :as energy]
            [cn.academy.config.mod-config :as config]))

(defn create-energy-storage
  "Create a new energy storage with specified capacity and transfer rates.
   
   Parameters:
   - capacity: Maximum energy storage capacity in FE
   - max-receive: Maximum energy receive rate per tick
   - max-extract: Maximum energy extraction rate per tick
   
   Returns: An energy storage capability instance"
  [& {:keys [capacity max-receive max-extract]
      :or {capacity 10000
           max-receive 1000
           max-extract 1000}}]
  (cap/create-energy-storage
    :capacity capacity
    :max-receive max-receive
    :max-extract max-extract))

(defn find-energy-receivers
  "Find all energy-capable tile entities within range of a position.
   
   Parameters:
   - world: The world instance
   - pos: Position map with :x :y :z keys
   - range: Search radius in blocks
   
   Returns: Sequence of tile entities that can receive energy"
  [world pos range]
  (energy/find-nearby-energy-tiles world pos range))

(defn transfer-energy
  "Transfer energy between two tile entities.
   
   Parameters:
   - source: Source tile entity with energy capability
   - target: Target tile entity with energy capability
   - amount: Maximum amount of energy to transfer
   
   Returns: Amount of energy actually transferred, or nil if transfer failed"
  [source target amount]
  (energy/transfer-energy source target amount))

(defn get-config
  "Get mod configuration values.
   
   Returns: Map of configuration values"
  []
  (config/get-wireless-matrix-config))