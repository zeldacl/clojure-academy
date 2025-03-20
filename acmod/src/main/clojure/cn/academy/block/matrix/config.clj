(ns cn.academy.block.matrix.config)

(def default-config
  {:energy {:base-capacity 100000
            :base-transfer 1000
            :core-multiplier 2.0
            :plate-multiplier 1.5
            :transfer-efficiency 0.95
            :base-consumption 10}
   :network {:base-range 16.0
             :range-multiplier 2.0
             :max-nodes 8
             :base-bandwidth 1000}
   :block {:material :iron
           :hardness 3.0
           :resistance 15.0
           :light-level 0}})

(defprotocol IMatrixConfig
  (get-energy-config [this])
  (get-network-config [this])
  (get-block-config [this])
  (get-energy-capacity [this core-level plate-count])
  (get-energy-transfer [this core-level])
  (get-network-range [this core-level])
  (get-network-bandwidth [this core-level]))

(defrecord MatrixConfig [config-atom]
  IMatrixConfig
  (get-energy-config [_]
    (:energy @config-atom))
  
  (get-network-config [_]
    (:network @config-atom))
  
  (get-block-config [_]
    (:block @config-atom))
  
  (get-energy-capacity [this core-level plate-count]
    (let [{:keys [base-capacity core-multiplier plate-multiplier]} (get-energy-config this)]
      (* base-capacity
         (Math/pow core-multiplier core-level)
         (Math/pow plate-multiplier plate-count))))
  
  (get-energy-transfer [this core-level]
    (let [{:keys [base-transfer core-multiplier]} (get-energy-config this)]
      (* base-transfer (Math/pow core-multiplier core-level))))
  
  (get-network-range [this core-level]
    (let [{:keys [base-range range-multiplier]} (get-network-config this)]
      (* base-range (Math/pow range-multiplier core-level))))
  
  (get-network-bandwidth [this core-level]
    (let [{:keys [base-bandwidth]} (get-network-config this)]
      (* base-bandwidth core-level))))

(defn create-config
  "Create new matrix configuration"
  ([]
   (create-config default-config))
  ([initial-config]
   (->MatrixConfig (atom initial-config))))