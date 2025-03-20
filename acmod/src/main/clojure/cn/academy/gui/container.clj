(ns cn.academy.gui.container
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core]
            [clojure.tools.logging :as log]))

(defrecord BaseContainer [inventory slots]
  IContainer
  (get-slots [this]
    slots)
  
  (get-slot [this index]
    (get slots index))
    
  (can-interact-with [this player]
    true)
    
  (transfer-stack-in-slot [this player slot-id]
    (when-let [slot (get-slot this slot-id)]
      (let [stack (get-stack-in-slot (:inventory this) slot-id)]
        (if (and stack (is-item-valid? slot stack))
          (merge-stack slot stack)
          false))))
          
  (detect-changes [this]
    (doseq [slot slots]
      (on-slot-changed slot)))
      
  (on-closed [this player]
    ;; Default close handler
    ))

(defrecord MachineContainer [inventory machine-inv player-inv hotbar-inv]
  IContainer
  (get-slots [this]
    (concat (:slots machine-inv)
            (:slots player-inv)
            (:slots hotbar-inv)))
            
  (get-slot [this index]
    (cond
      (< index (get-size machine-inv))
      (get-slot machine-inv index)
      
      (< index (+ (get-size machine-inv) 
                  (get-size player-inv)))
      (get-slot player-inv (- index (get-size machine-inv)))
      
      :else
      (get-slot hotbar-inv (- index (+ (get-size machine-inv)
                                      (get-size player-inv))))))
                                      
  (can-interact-with [this player]
    true)
    
  (transfer-stack-in-slot [this player slot-id]
    (let [slot (get-slot this slot-id)]
      (when slot
        (transfer-stack slot player))))
        
  (detect-changes [this]
    (doseq [inv [machine-inv player-inv hotbar-inv]]
      (detect-sync-changes inv)))
      
  (on-closed [this player]
    (doseq [inv [machine-inv player-inv hotbar-inv]]
      (.on-closed inv player))))

(defn create-container [inventory]
  (->BaseContainer inventory
                  (vec (for [i (range (get-size inventory))]
                         (create-slot i 
                                    (* (mod i 9) 18)
                                    (* (quot i 9) 18))))))
                                    
(defn create-machine-container [machine-inv player-inv]
  (let [hotbar-start (+ (get-size machine-inv)
                       (get-size player-inv))]
    (->MachineContainer machine-inv 
                       player-inv
                       (take 9 player-inv)
                       (drop hotbar-start player-inv))))