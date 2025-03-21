(ns cn.academy.tech-system.energy-system.block.matrix
  (:require [cn.academy.tech-system.energy-system.network.wireless :as network]
            [cn.academy.tech-system.energy-system.network.state :as network-state]
            [cn.academy.tech-system.energy-system.security.manager :as security]
            [mcmod.nbt :as nbt]))

(defprotocol IBlockMatrix
  (get-network-id [this])
  (get-matrix-type [this])
  (get-max-nodes [this])
  (get-transfer-rate [this])
  (get-wireless-range [this]))

(defrecord BlockMatrixHandler [matrix-id properties node-config]
  IBlockMatrix
  (get-network-id [_] matrix-id)
  
  (get-matrix-type [_] 
    (:type @properties))
  
  (get-max-nodes [_]
    (get-in @node-config [:limits :max-nodes]))
  
  (get-transfer-rate [_]
    (get-in @node-config [:energy :transfer-rate]))
  
  (get-wireless-range [_]
    (get-in @node-config [:limits :range]))
  
  network/IWirelessMatrix
  (create-network [this]
    (network-state/create-network! matrix-id this))

  (get-network [_]
    (network-state/get-network matrix-id))
  
  (get-network-name [_]
    (get-in @properties [:network :name]))
  
  (set-network-name! [_ name]
    (swap! properties assoc-in [:network :name] name))
  
  (get-range [this]
    (get-wireless-range this))
  
  (get-capacity [this]
    (get-max-nodes this))
  
  (get-bandwidth [this]
    (get-transfer-rate this)))

(defn create-matrix [matrix-id type config]
  (->BlockMatrixHandler
    matrix-id
    (atom {:type type
           :network {:name (str "Network " matrix-id)}})
    (atom config)))