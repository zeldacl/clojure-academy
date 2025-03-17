(ns cn.academy.block.matrix-config)

(defprotocol IMatrixConfig
  (get-base-capacity [this])
  (get-capacity-multiplier [this])
  (get-base-bandwidth [this])
  (get-bandwidth-multiplier [this])
  (get-base-range [this])
  (get-range-multiplier [this])
  (reload-config! [this]))

(defrecord MatrixConfiguration [config-state]
  IMatrixConfig
  (get-base-capacity [_]
    (get @config-state :base-capacity 1000))
  
  (get-capacity-multiplier [_]
    (get @config-state :capacity-multiplier 8.0))
  
  (get-base-bandwidth [_]
    (get @config-state :base-bandwidth 100))
  
  (get-bandwidth-multiplier [_]
    (get @config-state :bandwidth-multiplier 60.0))
  
  (get-base-range [_]
    (get @config-state :base-range 16))
  
  (get-range-multiplier [_]
    (get @config-state :range-multiplier 24.0))
  
  (reload-config! [_]
    (reset! config-state
            {:base-capacity 1000
             :capacity-multiplier 8.0
             :base-bandwidth 100
             :bandwidth-multiplier 60.0
             :base-range 16
             :range-multiplier 24.0})))

(defn create-matrix-config []
  (->MatrixConfiguration (atom {})))