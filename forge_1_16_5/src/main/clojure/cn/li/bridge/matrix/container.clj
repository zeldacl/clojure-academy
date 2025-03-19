(ns cn.li.bridge.matrix.container
  (:require [cn.li.bridge.matrix.api :as api]
            [cn.li.bridge.gui.api :as gui]
            [cn.li.bridge.inventory.api :as inv])
  (:import [net.minecraft.inventory container.Container Slot]
           [net.minecraft.entity.player PlayerEntity]
           [net.minecraft.item ItemStack]))

;; Slot positions in container
(def ^:private slot-positions
  {:core {:x 76 :y 35}
   :plate-1 {:x 56 :y 15}
   :plate-2 {:x 96 :y 15}
   :plate-3 {:x 76 :y 55}})

(defrecord MatrixContainer [matrix player]
  gui/IGuiContainer
  (get-slots [_]
    [{:id :core 
      :type :core
      :pos (:core slot-positions)}
     {:id :plate-1
      :type :plate
      :pos (:plate-1 slot-positions)}
     {:id :plate-2 
      :type :plate
      :pos (:plate-2 slot-positions)}
     {:id :plate-3
      :type :plate
      :pos (:plate-3 slot-positions)}])
  
  (is-valid? [_ player]
    (when (= (.getUUID player) (:owner @(:state-atom matrix)))
      true))
  
  (on-closed [_ player]
    (api/sync-with-client matrix))
  
  (sync-data [_]
    (api/sync-with-client matrix))
  
  (on-button-clicked [_ player button-id]
    (case button-id
      :connect (when-let [network-id (api/get-network-id matrix)]
                (api/connect-to-network matrix network-id nil))
      :disconnect (api/leave-network matrix)
      nil))

  inv/IInventory
  (get-size [_]
    4) ; Core slot + 3 plate slots
  
  (get-stack [_ slot]
    (let [state @(:state-atom matrix)]
      (case slot
        0 (:core state)
        (get-in state [:plates (dec slot)]))))
  
  (set-stack [this slot stack]
    (let [valid? (case slot
                  0 (api/validate-core matrix stack)  
                  (api/validate-plate matrix stack))]
      (when valid?
        (swap! (:state-atom matrix) 
               (fn [state]
                 (if (zero? slot)
                   (assoc state :core stack)
                   (assoc-in state [:plates (dec slot)] stack)))))))
  
  (remove-stack [this slot amount]
    (let [current (inv/get-stack this slot)]
      (when current
        (inv/set-stack this slot nil)
        current)))
  
  (is-empty? [_]
    (let [state @(:state-atom matrix)]
      (and (nil? (:core state))
           (every? nil? (:plates state)))))
  
  (mark-dirty [_]
    (api/sync-with-client matrix)))

(defn create-container [matrix player]
  (->MatrixContainer matrix player))

(defn register-container-type [registry]
  (gui/register-gui registry 
                    "matrix_container"
                    {:create-fn create-container
                     :validate-fn #(api/is-formed? %)}))