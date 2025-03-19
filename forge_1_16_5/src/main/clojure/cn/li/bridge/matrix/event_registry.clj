(ns cn.li.bridge.matrix.event-registry
  (:require [cn.li.bridge.matrix.events :as events]
            [cn.li.bridge.matrix.network-events :as network-events]
            [cn.li.bridge.matrix.energy-events :as energy-events]
            [clojure.tools.logging :as log])
  (:import [net.minecraftforge.common MinecraftForge]
           [net.minecraftforge.eventbus.api IEventBus]))

(defprotocol IMatrixEventRegistry
  "Registry for managing matrix event handlers"
  (register-matrix [this matrix] "Register event handlers for a matrix")
  (unregister-matrix [this matrix] "Unregister event handlers for a matrix"))

(defrecord MatrixEventRegistry [handlers-atom]
  IMatrixEventRegistry
  (register-matrix [_ matrix]
    (let [core-handler (events/create-event-dispatcher matrix)
          network-handler (network-events/create-network-handler matrix)
          energy-handler (energy-events/create-energy-handler matrix)
          event-bus (MinecraftForge/EVENT_BUS)]
      
      (events/register-events! core-handler event-bus)
      (network-events/register-network-events! network-handler event-bus)
      (energy-events/register-energy-events! energy-handler event-bus)
      
      (swap! handlers-atom assoc matrix
             {:core core-handler
              :network network-handler
              :energy energy-handler})
      
      (log/info "Registered event handlers for matrix at" (str matrix))))
  
  (unregister-matrix [_ matrix]
    (when-let [{:keys [core network energy]} (get @handlers-atom matrix)]
      (let [event-bus (MinecraftForge/EVENT_BUS)]
        (.unregister event-bus core)
        (.unregister event-bus network)
        (.unregister event-bus energy)
        (swap! handlers-atom dissoc matrix)
        (log/info "Unregistered event handlers for matrix at" (str matrix))))))

(defn create-registry []
  (->MatrixEventRegistry (atom {})))

(def ^:private registry-instance (atom nil))

(defn get-registry []
  (when-not @registry-instance
    (reset! registry-instance (create-registry)))
  @registry-instance)