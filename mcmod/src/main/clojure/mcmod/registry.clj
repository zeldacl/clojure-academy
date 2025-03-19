(ns mcmod.registry
  (:require [mcmod.protocols :refer :all]))

(defonce ^:private mod-registries (atom {}))

(defrecord ModRegistry [mod-id]
  IModRegistry
  (register-block! [_ id block]
    (swap! mod-registries update-in [mod-id :blocks] assoc id block))

  (register-item! [_ id item]
    (swap! mod-registries update-in [mod-id :items] assoc id item))

  (register-tile-entity! [_ id block te]
    (swap! mod-registries update-in [mod-id :tile-entities] assoc id {:block block :te te}))

  (register-gui! [_ id container screen]
    (swap! mod-registries update-in [mod-id :guis] assoc id {:container container :screen screen})))

(defn create-registry [mod-id]
  (->ModRegistry mod-id))

(defn get-registered-blocks [mod-id]
  (get-in @mod-registries [mod-id :blocks]))

(defn get-registered-items [mod-id]
  (get-in @mod-registries [mod-id :items]))

(defn get-registered-tile-entities [mod-id]
  (get-in @mod-registries [mod-id :tile-entities]))

(defn get-registered-guis [mod-id]
  (get-in @mod-registries [mod-id :guis]))

(defn get-registered-mods []
  (keys @mod-registries))