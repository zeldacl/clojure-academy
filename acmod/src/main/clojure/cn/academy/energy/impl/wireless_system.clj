(ns cn.academy.energy.impl.wireless-system
  (:require [cn.academy.energy.impl.wireless-world-data :as world-data]
            [cn.academy.energy.api.wireless-events :as events])
  (:import [net.minecraftforge.event TickEvent$ServerTickEvent TickEvent$Phase]
           [net.minecraftforge.eventbus.api IEventBus]
           [net.minecraft.world World]))

(def ^:private instance (atom nil))

(defprotocol IWirelessSystem
  (register-event-bus! [this event-bus])
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
  (register-event-bus! [_ event-bus]
    (.addListener event-bus 
      (proxy [Consumer] []
        (accept [event]
          (when (instance? TickEvent$ServerTickEvent event)
            (on-server-tick this event))))))

  (on-server-tick [_ event]
    (when (= (.phase event) TickEvent$Phase/END)
      (doseq [world (.getWorlds (MinecraftServer/getInstance))]
        (when-let [data (world-data/get-non-create world)]
          (.tick data)))))
  
  (on-create-network [_ event]
    (let [world (.getWorld (.tile event))
          data (world-data/get world)]
      (when-not (world-data/create-network! data (.matrix event) (.ssid event) (.password event))
        (.setCanceled event true))))
  
  (on-destroy-network [_ event]
    (when-let [network (.network event)]
      (.dispose network)
      (.markDirty (.world-data network))))
  
  (on-link-node [_ event]
    (let [network (.network event)]
      (when-not (.addNode network (.node event) (.password event))
        (.setCanceled event true))))
  
  (on-unlink-node [_ event]
    (let [network (.network event)]
      (.removeNode network (.node event))))
  
  (on-link-user [_ event]
    (let [node (.node event)
          data (world-data/get (.getWorld (.tile event)))]
      (when-let [conn (world-data/get-or-create-connection data node)]
        (.addUser conn (.user event)))))
  
  (on-unlink-user [_ event]
    (let [user (.user event)
          world (.getWorld (.tile event))
          data (world-data/get world)]
      (when-let [conn (world-data/get-connection-by-user data user)]
        (.removeUser conn user))))
  
  (on-change-password [_ event]
    (let [network (.network event)]
      (when-not (.resetPassword network (.oldPass event) (.newPass event))
        (.setCanceled event true)))))

(defn init! []
  (reset! instance (->WirelessSystem)))

(defn get-instance []
  @instance)