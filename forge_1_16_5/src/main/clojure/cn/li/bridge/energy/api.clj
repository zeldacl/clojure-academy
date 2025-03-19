(ns cn.li.bridge.energy.api)

(defprotocol IEnergyStorage
  "Core energy storage functionality"
  (receive-energy [this amount simulate] "Receive energy")
  (extract-energy [this amount simulate] "Extract energy")
  (get-energy-stored [this] "Get stored energy")
  (get-energy-capacity [this] "Get max capacity")
  (can-receive? [this] "Check if can receive")
  (can-extract? [this] "Check if can extract"))

(defprotocol IEnergyProvider 
  "Energy provider functionality"
  (provide-energy [this amount simulate] "Provide energy")
  (get-provided-energy [this] "Get provided energy"))

(defprotocol IEnergyConsumer
  "Energy consumer functionality"
  (consume-energy [this amount simulate] "Consume energy")
  (get-energy-need [this] "Get energy needed"))

(defn create-energy-impl []
  nil) ;; Implement in bridge