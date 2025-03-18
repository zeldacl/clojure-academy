(ns cn.academy.block.matrix-config)

(defprotocol IMatrixConfig
  "Protocol for matrix configuration"
  (get-capacity-multiplier [this]
    "Get energy capacity multiplier")
  (get-bandwidth-multiplier [this]
    "Get bandwidth multiplier")
  (get-range-multiplier [this]
    "Get range multiplier")
  (get-formation-range [this]
    "Get maximum formation range")
  (get-base-consumption [this]
    "Get base energy consumption")
  (get-transfer-efficiency [this]
    "Get energy transfer efficiency"))

(defrecord MatrixConfig [config]
  IMatrixConfig
  (get-capacity-multiplier [_]
    (:capacity-multiplier config 10000.0))
  
  (get-bandwidth-multiplier [_]
    (:bandwidth-multiplier config 100.0))
  
  (get-range-multiplier [_]
    (:range-multiplier config 10.0))
  
  (get-formation-range [_]
    (:formation-range config 3))
  
  (get-base-consumption [_]
    (:base-consumption config 20.0))
  
  (get-transfer-efficiency [_]
    (:transfer-efficiency config 0.9)))

(def default-config
  {:capacity-multiplier 10000.0  ; Energy capacity per core level
   :bandwidth-multiplier 100.0   ; Transfer rate per core level squared
   :range-multiplier 10.0        ; Range per sqrt of core level
   :formation-range 3            ; Max blocks between matrix components
   :base-consumption 20.0        ; Base energy consumption per tick
   :transfer-efficiency 0.9})    ; Energy transfer efficiency (90%)

(defn create-config 
  "Create matrix configuration with optional overrides"
  [& {:as overrides}]
  (->MatrixConfig (merge default-config overrides)))