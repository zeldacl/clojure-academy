(ns cn.academy.block.matrix-gui-wireless
  (:require [cn.academy.block.matrix :as matrix]
            [cn.academy.block.matrix-container :as container]))

(defprotocol IWirelessGui
  (draw-wireless-info [this])
  (draw-capacity-bar [this])
  (draw-bandwidth-info [this])
  (draw-range-info [this]))

(defprotocol IWirelessGuiState
  (update-wireless-state! [this])
  (handle-network-sync! [this network-info]))

(defrecord WirelessGuiHandler [matrix gui-state]
  IWirelessGui
  (draw-wireless-info [_]
    (when (matrix/working? matrix)
      {:type :info-panel
       :elements [{:type :label :text "Wireless Matrix"}
                 {:type :separator}
                 {:type :property :key "Owner" :value (matrix/get-placer-name matrix)}
                 {:type :property :key "Range" :value (format "%.0f" (matrix/get-range matrix))}
                 {:type :property :key "Bandwidth" :value (str (matrix/get-bandwidth matrix) " IF/T")}]}))
  
  (draw-capacity-bar [_]
    {:type :capacity-bar
     :value (matrix/get-capacity matrix)
     :max-value (matrix/get-capacity matrix)})
  
  (draw-bandwidth-info [_]
    {:type :bandwidth-display
     :value (matrix/get-bandwidth matrix)})
  
  (draw-range-info [_]
    {:type :range-display
     :value (matrix/get-range matrix)})

  IWirelessGuiState
  (update-wireless-state! [_]
    (swap! gui-state assoc
           :capacity (matrix/get-capacity matrix)
           :bandwidth (matrix/get-bandwidth matrix)
           :range (matrix/get-range matrix)))
  
  (handle-network-sync! [_ network-info]
    (swap! gui-state merge network-info)))

(defn create-wireless-gui [matrix]
  (->WirelessGuiHandler matrix (atom {})))