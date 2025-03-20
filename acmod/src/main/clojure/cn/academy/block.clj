(ns cn.academy.block
  (:require [cn.academy.block.component :as component]
            [cn.academy.block.registry :as registry]
            [mcmod.block.protocols :refer [IBlockComponent]]
            [clojure.tools.logging :as log])
  (:import [net.minecraft.block.material Material]))

;; Core block definitions with properties
(def block-definitions
  {"ability_interferer" {:material Material/IRON
                        :hardness 3.5
                        :resistance 8.0
                        :has-tile-entity true}
   
   "cat_engine" {:material Material/IRON
                 :hardness 4.0
                 :resistance 8.0
                 :has-tile-entity true}
   
   "crystal_ore" {:material Material/ROCK
                  :hardness 3.0
                  :resistance 5.0
                  :light-level 0.0
                  :harvest-level 2}
   
   "constraint_metal" {:material Material/IRON
                      :hardness 4.0
                      :resistance 10.0
                      :harvest-level 2}
   
   "machine_frame" {:material Material/IRON
                   :hardness 5.0
                   :resistance 10.0
                   :harvest-level 1}
   
   "matrix" {:material Material/IRON
             :hardness 4.0
             :resistance 8.0
             :has-tile-entity true
             :harvest-level 1
             :event-handlers {:on-block-activated (fn [world pos state player]
                                                  (when-not (.isSneaking player)
                                                    (registry/open-gui player world pos)
                                                    true))
                            :on-block-placed (fn [world pos state placer stack]
                                             (when (instance? net.minecraft.entity.player.EntityPlayer placer)
                                               {:placer-name (.getName placer)}))}}
   
   "node_basic" {:material Material/IRON
                 :hardness 3.0
                 :resistance 5.0
                 :has-tile-entity true
                 :harvest-level 1}
   
   "node_standard" {:material Material/IRON
                   :hardness 3.5
                   :resistance 6.0
                   :has-tile-entity true
                   :harvest-level 2}
   
   "node_advanced" {:material Material/IRON
                   :hardness 4.0
                   :resistance 8.0
                   :has-tile-entity true
                   :harvest-level 2}
   
   "phase_gen" {:material Material/IRON
                :hardness 3.5
                :resistance 8.0
                :has-tile-entity true
                :harvest-level 1}
   
   "processor" {:material Material/IRON
                :hardness 4.0
                :resistance 20.0
                :light-level 5
                :has-tile-entity true
                :render-type :cutout
                :opaque? false}})

;; Block state management
(defrecord BlockState [id properties components]
  IBlockComponent
  (update! [_]
    (doseq [component (vals @components)]
      (update! component)))
  
  (get-capability [_ type side]
    (some #(get-capability % type side) (vals @components))))

;; Block creation helpers
(defn create-block 
  "Create a new block instance with given properties"
  [id & {:as props}]
  (let [definition (merge (get block-definitions id {}) props)]
    (registry/create-block id definition)))

(defn create-item-block
  "Create an ItemBlock for a block"
  [block]
  (registry/create-item-block block))

(defn register-blocks! 
  "Register all defined blocks with the given registry"
  [registry]
  (doseq [[id props] block-definitions]
    (let [block (create-block id props)]
      (register-block! registry id block)
      (when (:has-tile-entity props)
        (register-tile-entity! registry 
                              id
                              (registry/create-tile-entity id props))))))

;; Re-export commonly used functions
(def register-block! registry/register-block!)
(def register-tile-entity! registry/register-tile-entity!)
(def register-container! registry/register-container!)
(def register-event-handler! registry/register-event-handler!)
(def register-item! registry/register-item!)