(ns mcmod.mod-registry
  (:require [mcmod.protocols :refer :all]))

(defprotocol IModRegistry
  "Mod-specific registry functionality"
  (register-mod [this mod-id] "Register a new mod")
  (get-mod-blocks [this mod-id] "Get blocks registered for a specific mod")
  (get-mod-items [this mod-id] "Get items registered for a specific mod")
  (get-mod-tile-entities [this mod-id] "Get tile entities registered for a specific mod"))

(defrecord ModSpecificRegistry []
  IModRegistry
  (register-mod [this mod-id]
    (swap! (:mods this) conj mod-id))
  
  (get-mod-blocks [this mod-id]
    (get-in @(:mod-blocks this) [mod-id]))
  
  (get-mod-items [this mod-id]
    (get-in @(:mod-items this) [mod-id]))
  
  (get-mod-tile-entities [this mod-id]
    (get-in @(:mod-tile-entities this) [mod-id])))

(defn create-mod-registry []
  (->ModSpecificRegistry
    {:mods (atom #{})
     :mod-blocks (atom {})
     :mod-items (atom {})
     :mod-tile-entities (atom {})}))