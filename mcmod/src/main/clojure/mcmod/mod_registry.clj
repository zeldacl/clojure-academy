(ns mcmod.mod-registry
  (:require [mcmod.protocols :refer :all]))

(defprotocol IModRegistry
  "Mod-specific registry functionality"
  (register-mod [this mod-id] "Register a new mod")
  (get-mod-blocks [this mod-id] "Get blocks registered for a specific mod")
  (get-mod-items [this mod-id] "Get items registered for a specific mod")
  (get-mod-tile-entities [this mod-id] "Get tile entities registered for a specific mod"))

(defrecord ModSpecificRegistry [registry-state]
  IModRegistry
  (register-mod [this mod-id]
    (swap! registry-state update :mods conj mod-id))
  
  (get-mod-blocks [this mod-id]
    (get-in @registry-state [:blocks mod-id]))
  
  (get-mod-items [this mod-id]
    (get-in @registry-state [:items mod-id]))
  
  (get-mod-tile-entities [this mod-id]
    (get-in @registry-state [:tile-entities mod-id])))

(defn create-mod-registry []
  (->ModSpecificRegistry
    (atom {:mods #{}
           :blocks {}
           :items {}
           :tile-entities {}})))