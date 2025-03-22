(ns cn.academy.blocks.block-matrix.events
  "Event handling for the wireless matrix block."
  (:require [cn.academy.blocks.block-matrix.protocols :refer :all]
            [cn.academy.blocks.block-matrix.utils :as utils]
            [clojure.tools.logging :as log]))

;; Enhanced matrix event handler protocol
(defprotocol IMatrixEventHandler
  "Handles block-related events for the matrix"
  (on-block-place [this world pos placer] "Handle block placement event")
  (on-block-break [this world pos] "Handle block destruction event")
  (on-block-activate [this world pos player hand facing hit] "Handle block activation event")
  (on-network-join [this world pos network-id password] "Handle network join event")
  (on-network-leave [this world pos] "Handle network leave event")
  (on-tick [this world pos] "Handle tick update event")
  (on-neighbor-change [this world pos neighbor-pos] "Handle neighbor block change"))

;; Matrix event dispatcher implementation
(defrecord MatrixEventDispatcher [state network energy]
  IMatrixEventHandler
  (on-block-place [_ world pos placer]
    (utils/with-error-handling "Error handling block place event"
      (when placer
        (set-owner state (.getName placer))
        true)))
  
  (on-block-break [_ world pos]
    (utils/with-error-handling "Error handling block break event"
      (when (is-formed? state)
        (leave-network network)
        true)))
  
  (on-block-activate [_ world pos player hand facing hit]
    (utils/with-error-handling "Error handling block activate event"
      (when (and (not (.isSneaking player))
                (can-interact? state player))
        {:type :open-gui
         :gui-id :matrix
         :pos pos})))
  
  (on-network-join [_ world pos network-id password]
    (utils/with-error-handling "Error handling network join event"
      (when (is-valid? state)
        (join-network network network-id password))))
  
  (on-network-leave [_ world pos]
    (utils/with-error-handling "Error handling network leave event"
      (leave-network network)))
  
  (on-tick [_ world pos]
    (utils/with-error-handling "Error handling tick event"
      ;; Process state updates
      (on-tick! state)
      
      ;; Process network updates
      (when (is-formed? state)
        (if (is-active? state)
          ;; Handle active matrix updates
          (let [consumption (utils/calculate-energy-consumption 
                              (get-core-level state) 
                              (count (get-connected-nodes network)))]
            ;; Consume energy for operation
            (if (has-energy-for-operation? energy consumption)
              (do
                (remove-energy energy consumption)
                true)
              ;; Not enough energy, deactivate
              (do
                (set-active state false)
                false)))
          ;; Try to activate if possible
          (when (and (is-formed? state)
                     (has-energy-for-operation? energy 
                       (utils/calculate-activation-energy (get-core-level state))))
            (set-active state true)
            true)))))
  
  (on-neighbor-change [_ world pos neighbor-pos]
    (utils/with-error-handling "Error handling neighbor change event"
      ;; Check formation status
      (let [was-formed (is-formed? state)
            is-formed (can-form? state)]
        (when (not= was-formed is-formed)
          ;; Handle formation state change
          (utils/process-form-state-change state was-formed is-formed))))))

;; Event utility functions
(defn create-event-dispatcher
  "Create a new matrix event dispatcher"
  [state network energy]
  (->MatrixEventDispatcher state network energy))

;; Standard event translation for different systems
(defn map-event 
  "Map Minecraft event to matrix event"
  [event-type matrix-events & args]
  (condp = event-type
    :place (apply on-block-place matrix-events args)
    :break (apply on-block-break matrix-events args)
    :activate (apply on-block-activate matrix-events args)
    :tick (apply on-tick matrix-events args)
    :neighbor-change (apply on-neighbor-change matrix-events args)
    nil))