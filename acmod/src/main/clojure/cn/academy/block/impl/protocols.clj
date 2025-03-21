(ns cn.academy.block.impl.protocols
  (:require [cn.academy.block.behavior :as behavior]
            [clojure.tools.logging :as log]))

;; Implement mcmod block protocols
(extend-protocol mcmod.block/IBlock
  clojure.lang.IPersistentMap
  (get-properties [this]
    (:properties this))
  
  (on-placed [this world pos state placer]
    (when-let [behaviors (:behaviors this)]
      (behaviors :on-placed world pos state placer)))
      
  (on-broken [this world pos]
    (when-let [behaviors (:behaviors this)]
      (behaviors :on-broken world pos))))

;; Implement mcmod machine protocols  
(extend-protocol mcmod.machine/IMachine
  clojure.lang.IPersistentMap
  (get-energy [this]
    (get-in this [:state :energy] 0))
    
  (add-energy! [this amount]
    (swap! (:state this) update :energy + amount))
    
  (can-process? [this recipe]
    (and (get-in this [:state :active])
         (>= (get-in this [:state :energy]) 
             (get-in recipe [:energy-cost] 0)))))

;; Implement mcmod multiblock protocols
(extend-protocol mcmod.multiblock/IMultiblockMember
  clojure.lang.IPersistentMap
  (get-controller [this]
    (get-in this [:member :controller]))
    
  (set-controller! [this controller]
    (swap! (get-in this [:member :state-atom]) 
           assoc :controller controller))
           
  (can-connect? [this other]
    (behavior/apply-behavior! 
      (get-in this [:member :type])
      :can-connect?
      other)))