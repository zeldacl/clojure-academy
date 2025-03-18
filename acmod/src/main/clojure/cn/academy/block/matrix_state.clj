(ns cn.academy.block.matrix-state
  (:require [cn.academy.block.matrix :as matrix]
            [cn.academy.block.matrix-config :as config]
            [cn.academy.block.matrix-inventory :as inv]))

(defprotocol IMatrixState 
  (init-state! [this])
  (get-plate-info [this])
  (get-core-info [this])
  (on-plate-changed [this slot item])
  (on-core-changed [this item])
  (update-capability-state [this])
  (serialize-state [this])
  (deserialize-state [this data])
  "Protocol for matrix state management"
  (is-formed? [this]
    "Check if matrix structure is formed")
  (try-form! [this validator]
    "Try to form matrix structure")
  (break! [this]
    "Break matrix structure")
  (get-core-level [this]
    "Get core energy level")
  (get-plate-count [this]
    "Get number of installed plates")
  (get-energy-stored [this]
    "Get stored energy")
  (get-energy-capacity [this]
    "Get energy capacity")
  (get-position [this]
    "Get matrix position")
  (set-position! [this pos]
    "Set matrix position"))

(defrecord MatrixState [matrix config state-atom inventory]
  IMatrixState
  (init-state! [_]
    (reset! state-atom
            {:plates [nil nil nil]
             :core nil
             :total-capacity 0
             :current-bandwidth 0
             :max-range 0
             :formed? false
             :energy 0.0
             :position nil}))
  
  (get-plate-info [_]
    (select-keys @state-atom [:plates]))
  
  (get-core-info [_]
    {:core (:core @state-atom)
     :level (matrix/get-core-level matrix)})
  
  (on-plate-changed [this slot item]
    (swap! state-atom update :plates assoc slot item)
    (update-capability-state this))
  
  (on-core-changed [this item]
    (swap! state-atom assoc :core item)
    (update-capability-state this))
  
  (update-capability-state [_]
    (let [core-level (matrix/get-core-level matrix)
          plate-count (matrix/get-plate-count matrix)]
      (when (and (pos? core-level) (= plate-count 3))
        (swap! state-atom merge
               {:total-capacity (* core-level (config/get-capacity-multiplier config))
                :current-bandwidth (* core-level core-level (config/get-bandwidth-multiplier config))
                :max-range (* (Math/sqrt core-level) (config/get-range-multiplier config))}))))
  
  (serialize-state [_]
    @state-atom)
  
  (deserialize-state [this data]
    (reset! state-atom data))
  
  (is-formed? [_]
    (:formed? @state-atom))
  
  (try-form! [_ validator]
    (when (validator)
      (swap! state-atom assoc :formed? true)
      true))
  
  (break! [_]
    (swap! state-atom assoc :formed? false))
  
  (get-core-level [_]
    (if-let [core (inv/get-core-item inventory)]
      (:level core)
      0))
  
  (get-plate-count [_]
    (inv/get-plate-count inventory))
  
  (get-energy-stored [_]
    (:energy @state-atom))
  
  (get-energy-capacity [this]
    (* (get-core-level this) 10000.0))
  
  (get-position [_]
    (:position @state-atom))
  
  (set-position! [_ pos]
    (swap! state-atom assoc :position pos)))

(defn create-matrix-state [matrix config inventory]
  (->MatrixState matrix config (atom {:formed? false
                                      :energy 0.0
                                      :position nil})
                 inventory))

(defn get-state-data [state]
  {:formed? (is-formed? state)
   :energy (get-energy-stored state)
   :position (get-position state)})

(defn load-state-data! [state data]
  (let [{:keys [formed? energy position]} data]
    (swap! (:state-atom state)
           assoc
           :formed? formed?
           :energy energy
           :position position)))