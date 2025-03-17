(ns cn.academy.block.matrix-state
  (:require [cn.academy.block.matrix :as matrix]
            [cn.academy.block.matrix-config :as config]))

(defprotocol IMatrixState 
  (init-state! [this])
  (get-plate-info [this])
  (get-core-info [this])
  (on-plate-changed [this slot item])
  (on-core-changed [this item])
  (update-capability-state [this])
  (serialize-state [this])
  (deserialize-state [this data]))

(defrecord MatrixState [matrix config state-atom]
  IMatrixState
  (init-state! [_]
    (reset! state-atom
            {:plates [nil nil nil]
             :core nil
             :total-capacity 0
             :current-bandwidth 0
             :max-range 0}))
  
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
    (reset! state-atom data)))

(defn create-matrix-state [matrix config]
  (->MatrixState matrix config (atom {})))