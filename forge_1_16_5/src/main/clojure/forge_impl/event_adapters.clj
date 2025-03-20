(ns forge-impl.event-adapters
  (:require [mcmod.protocols :refer [IEventBus IServerEvents]])
  (:import [net.minecraftforge.eventbus.api IEventBus EventBus]
           [net.minecraftforge.fml.event.server ServerStoppingEvent ServerStartedEvent]))

(defrecord ForgeEventBus [^IEventBus bus]
  IEventBus
  (register-handler [_ handler]
    (.register bus handler))
  
  (post-event [_ event]
    (.post bus event)))

(defrecord ForgeServerEvents [^IEventBus bus]
  IServerEvents
  (on-stopping [_]
    (.post bus (ServerStoppingEvent.)))
  
  (on-started [_]
    (.post bus (ServerStartedEvent.))))

(defn create-event-bus []
  (->ForgeEventBus (EventBus.)))

(defn create-server-events [bus]
  (->ForgeServerEvents bus))