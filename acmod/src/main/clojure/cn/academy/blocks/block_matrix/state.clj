(ns cn.academy.blocks.block-matrix.state
  "Matrix state management with core functionality for the wireless matrix block."
  (:require [cn.academy.blocks.block-matrix.protocols :refer [IMatrixState IMatrixSecurity]]
            [cn.academy.blocks.block-matrix.utils :as utils]
            [cn.academy.blocks.block-matrix.inventory :as inventory]
            [clojure.tools.logging :as log]))

;; State management implementation
(defrecord MatrixState [config state-atom inventory]
  IMatrixState
  (is-active? [_]
    (:active @state-atom))
  
  (set-active [_ active?]
    (swap! state-atom assoc :active active?))
  
  (get-core-level [_]
    (let [core-item (inventory/get-core-item inventory)]
      (or (inventory/get-core-level core-item) 0)))
  
  (get-plate-count [_]
    (inventory/get-plate-count inventory))
  
  (is-formed? [this]
    (and (inventory/get-core-item inventory)
         (pos? (get-plate-count this))))
  
  (can-form? [this]
    (is-formed? this))
  
  (save-state [this]
    (merge @state-atom
           {:inventory (inventory/get-inventory-data inventory)}))
  
  (load-state [this data]
    (reset! state-atom (dissoc data :inventory))
    (when-let [inv-data (:inventory data)]
      (inventory/load-inventory-data! inventory inv-data)))
  
  ;; Security management
  IMatrixSecurity
  (get-owner [_]
    (:owner @state-atom))
  
  (set-owner [_ owner-id]
    (swap! state-atom assoc :owner owner-id))
  
  (can-interact? [this player]
    (let [owner (get-owner this)]
      (or (nil? owner) 
          (= owner (.getName player))
          (.isCreative player))))
  
  (set-password [_ password]
    (swap! state-atom assoc :password password))
  
  (get-password [_]
    (:password @state-atom))
  
  (validate-password [this password]
    (let [stored-pwd (get-password this)]
      (or (nil? stored-pwd)
          (empty? stored-pwd)
          (= stored-pwd password)))))

;; State utility functions
(defn update-network-state!
  "Update matrix state with network information"
  [state network-id connected?]
  (swap! (:state-atom state) assoc 
         :network-id network-id
         :connected connected?))

(defn on-tick!
  "Process matrix state updates for each tick"
  [state]
  (when (is-formed? state)
    (when-not (is-active? state)
      (set-active state true))))

(defn process-form-update!
  "Process changes to the matrix formation state"
  [state old-formed? new-formed?]
  (cond
    ;; Matrix became formed
    (and (not old-formed?) new-formed?)
    (utils/with-error-handling "Error processing matrix formation"
      (set-active state true)
      true)
    
    ;; Matrix became unformed
    (and old-formed? (not new-formed?))
    (utils/with-error-handling "Error processing matrix deformation"
      (set-active state false)
      true)
    
    :else false))

;; Matrix state serialization
(defn get-serialized-state
  "Get serialized state data suitable for NBT storage"
  [state]
  (let [base-state (save-state state)]
    {:active (:active base-state)
     :owner (:owner base-state)
     :password (:password base-state)
     :network-id (:network-id base-state)
     :connected (:connected base-state)
     :inventory (:inventory base-state)}))

;; Factory function
(defn create-matrix-state
  "Create a new matrix state manager"
  [config]
  (let [inv (inventory/create-inventory config)]
    (->MatrixState config 
                   (atom {:active false
                          :owner nil
                          :password ""
                          :network-id nil
                          :connected false})
                   inv)))