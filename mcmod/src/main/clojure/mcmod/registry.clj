(ns mcmod.registry
  (:require [mcmod.protocols :refer :all]
            [clojure.tools.logging :as log]))

;; Registry state
(def ^:private registries (atom {}))

;; Create a new registry instance for a mod
(defn create-registry [mod-id]
  (let [registry {:blocks {} 
                 :items {}
                 :tile-entities {}
                 :containers {}
                 :mod-id mod-id}]
    (swap! registries assoc mod-id registry)
    registry))

;; Get an existing registry
(defn get-registry [mod-id]
  (get @registries mod-id))

;; Register content
(defn register-block! [registry block-id block]
  (swap! registries update-in [(:mod-id registry) :blocks] assoc block-id block)
  block)

(defn register-item! [registry item-id item]
  (swap! registries update-in [(:mod-id registry) :items] assoc item-id item)
  item)

(defn register-tile-entity! [registry te-id te]
  (swap! registries update-in [(:mod-id registry) :tile-entities] assoc te-id te)
  te)

(defn register-container! [registry container-id container]
  (swap! registries update-in [(:mod-id registry) :containers] assoc container-id container)
  container)

;; Get registered content
(defn get-blocks [registry]
  (get-in @registries [(:mod-id registry) :blocks]))

(defn get-items [registry]
  (get-in @registries [(:mod-id registry) :items]))

(defn get-tile-entities [registry]
  (get-in @registries [(:mod-id registry) :tile-entities]))

(defn get-containers [registry]
  (get-in @registries [(:mod-id registry) :containers]))

;; Helper for creating common block types
(defn create-basic-block [& {:keys [material hardness resistance light-level]
                            :or {material :stone
                                 hardness 3.0
                                 resistance 5.0
                                 light-level 0}}]
  (reify IBlock
    (get-properties [_]
      {:material material
       :hardness hardness
       :resistance resistance
       :light-level light-level})
    
    (get-material [_] material)
    (get-hardness [_] hardness)
    (get-resistance [_] resistance)
    (get-light-level [_] light-level)
    
    (on-activated [_ pos data] false)
    (on-placed [_ pos data] nil)
    (on-removed [_ pos] nil)))

;; Helper for creating common item types  
(defn create-basic-item [& {:keys [max-stack damage-value creative-tab]
                           :or {max-stack 64
                                damage-value 0
                                creative-tab :misc}}]
  (reify IItem
    (get-max-stack-size [_] max-stack)
    (get-max-damage [_] damage-value)
    (is-repairable [_] false)
    (get-creative-tab [_] creative-tab)
    (on-item-use [_ world player hand] false)
    (on-right-click [_ world player hand] false)
    (on-hit-entity [_ target attacker] false)))