(ns cn.academy.energy.impl.wireless-system
  (:require [cn.academy.energy.impl.wireless-world-data :as world-data]
            [cn.academy.energy.api.wireless-events :as events]
            [mcmod.event :as event]
            [mcmod.world :as world]))

(def ^:private instance (atom nil))

(defprotocol IWirelessSystem
  (register-event-handlers! [this])
  (on-server-tick [this event])
  (on-create-network [this event])
  (on-destroy-network [this event])
  (on-link-node [this event])
  (on-unlink-node [this event])
  (on-link-user [this event])
  (on-unlink-user [this event])
  (on-change-password [this event]))

(defrecord WirelessSystem []
  IWirelessSystem
  (register-event-handlers! [this]
    (event/register-handler 
      {:server-tick (fn [e] (on-server-tick this e))
       :create-network (fn [e] (on-create-network this e))
       :destroy-network (fn [e] (on-destroy-network this e))
       :link-node (fn [e] (on-link-node this e))
       :unlink-node (fn [e] (on-unlink-node this e))
       :link-user (fn [e] (on-link-user this e))
       :unlink-user (fn [e] (on-unlink-user this e))
       :change-password (fn [e] (on-change-password this e))}))

  (on-server-tick [_ event]
    (when (event/is-end-phase? event)
      (doseq [world-obj (world/get-all-server-worlds)]
        (when-let [data (world-data/get-non-create world-obj)]
          (.tick data)))))
  
  (on-create-network [_ event]
    (let [world-obj (world/get-world (events/get-tile event))
          data (world-data/get world-obj)]
      (when-not (world-data/create-network! 
                  data 
                  (events/get-matrix event) 
                  (events/get-ssid event) 
                  (events/get-password event))
        (events/cancel! event))))
  
  (on-destroy-network [_ event]
    (when-let [network (events/get-network event)]
      (.dispose network)
      (world-data/mark-dirty! (:world-data network))))
  
  (on-link-node [_ event]
    (let [network (events/get-network event)]
      (when-not (.addNode network 
                        (events/get-node event) 
                        (events/get-password event))
        (events/cancel! event))))
  
  (on-unlink-node [_ event]
    (let [network (events/get-network event)]
      (.removeNode network (events/get-node event))))
  
  (on-link-user [_ event]
    (let [node (events/get-node event)
          world-obj (world/get-world (events/get-tile event))
          data (world-data/get world-obj)]
      (when-let [conn (world-data/get-or-create-connection data node)]
        (.addUser conn (events/get-user event)))))
  
  (on-unlink-user [_ event]
    (let [user (events/get-user event)
          world-obj (world/get-world (events/get-tile event))
          data (world-data/get world-obj)]
      (when-let [conn (world-data/get-connection-by-user data user)]
        (.removeUser conn user))))
  
  (on-change-password [_ event]
    (let [network (events/get-network event)]
      (when-not (.resetPassword network 
                             (events/get-old-password event) 
                             (events/get-new-password event))
        (events/cancel! event)))))

(defn init! []
  (let [system (->WirelessSystem)]
    (register-event-handlers! system)
    (reset! instance system)))

(defn get-instance []
  @instance)