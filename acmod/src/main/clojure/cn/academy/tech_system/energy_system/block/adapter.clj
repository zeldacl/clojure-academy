(ns cn.academy.tech-system.energy-system.block.adapter
  (:require [cn.academy.tech-system.energy-system.block.node :as node]
            [cn.academy.tech-system.energy-system.block.matrix :as matrix]
            [cn.academy.tech-system.energy-system.capability.wireless :as wireless]
            [mcmod.capabilities :as cap]))

(defprotocol IEnergyAdapter
  (to-wireless-node [this])
  (to-wireless-matrix [this])
  (to-energy-storage [this])
  (from-block [this block]))

(defrecord BlockEnergyAdapter []
  IEnergyAdapter
  (to-wireless-node [_ block]
    (when-let [energy-storage (cap/get-capability block "forge:energy")]
      (reify wireless/IWirelessNode
        (get-node-type [_] :block)
        (get-energy [_] (.get-energy-stored energy-storage))
        (get-max-energy [_] (.get-max-energy-stored energy-storage))
        (get-bandwidth [_] (get-in block [:config :energy :bandwidth] 1000.0))
        (get-range [_] (get-in block [:config :wireless :range] 16.0))
        (get-capacity [_] (get-in block [:config :wireless :max-connections] 4))
        (connect [this other] 
          (node/connect-to-network (.to-energy-storage this other)))
        (disconnect [this other]
          (node/disconnect-from-network (.to-energy-storage this other)))
        (can-connect? [_ other]
          (and (cap/has-capability? other "forge:energy")
               (.can-receive? energy-storage))))))
  
  (to-wireless-matrix [_ block]
    (when (cap/has-capability? block "academy:wireless_matrix")
      (let [matrix-id (str (random-uuid))
            config {:limits {:max-nodes 16
                           :range 32}
                   :energy {:transfer-rate 2000}}]
        (matrix/create-matrix matrix-id :block config))))
  
  (to-energy-storage [_ block]
    (when-let [node-storage (node/create-energy-storage block)]
      (reify cap/IEnergyStorage
        (receive-energy [_ amount simulate]
          (.receive-energy node-storage nil amount simulate))
        (extract-energy [_ amount simulate]
          (.extract-energy node-storage amount simulate))
        (get-energy-stored [_]
          (.get-energy-stored node-storage))
        (get-max-energy-stored [_]
          (.get-max-energy-stored node-storage))
        (can-extract? [_]
          (.can-extract? node-storage))
        (can-receive? [_]
          (.can-receive? node-storage)))))
  
  (from-block [this block]
    (cond
      (cap/has-capability? block "forge:energy")
      (to-wireless-node this block)
      
      (cap/has-capability? block "academy:wireless_matrix")
      (to-wireless-matrix this block)
      
      :else nil)))

(def adapter (->BlockEnergyAdapter))