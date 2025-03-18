(ns cn.academy.energy.energy-adapter)

(defprotocol IEnergyAdapter
  (receive-energy [this amount simulate])
  (extract-energy [this amount simulate])
  (get-stored-energy [this])
  (get-max-energy [this])
  (can-extract [this])
  (can-receive [this]))

(defrecord EnergyAdapter [energy-handler]
  IEnergyAdapter
  (receive-energy [_ amount simulate]
    (if (.can-receive? energy-handler)
      (let [actual amount]
        (when-not simulate
          (.receive-energy energy-handler actual false))
        actual)
      0))
  
  (extract-energy [_ amount simulate]
    (if (.can-extract? energy-handler)
      (let [actual amount]
        (when-not simulate
          (.extract-energy energy-handler actual false))
        actual)
      0))
  
  (get-stored-energy [_]
    (.get-stored-energy energy-handler))
  
  (get-max-energy [_]
    (.get-max-energy energy-handler))
  
  (can-extract [_]
    (.can-extract? energy-handler))
  
  (can-receive [_]
    (.can-receive? energy-handler)))

(defn create-adapter [energy-handler]
  (->EnergyAdapter energy-handler))