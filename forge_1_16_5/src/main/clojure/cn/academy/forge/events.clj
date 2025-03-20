(ns cn.academy.forge.events
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core]
            [cn.academy.forge.world :as world])
  (:import [net.minecraftforge.eventbus.api IEventBus EventPriority]
           [net.minecraftforge.event RegistryEvent$Register]
           [net.minecraftforge.event.world WorldEvent]
           [net.minecraftforge.event.server ServerStartingEvent ServerStoppingEvent]
           [net.minecraftforge.event.entity player.PlayerEvent]))

(defrecord ForgeEventBus [^IEventBus delegate handlers]
  IEventBus
  (register-handler [this handler]
    (swap! handlers conj handler))
    
  (post-event [this event]
    (doseq [handler @handlers]
      (handler event))))

(defrecord ForgeEventBridge [event-bus]
  IEventBridge
  (handle-world-load [this world]
    (post-event event-bus
                {:type :world-load
                 :world (world/wrap-world world)}))
                 
  (handle-world-unload [this world]
    (post-event event-bus  
                {:type :world-unload
                 :world (world/wrap-world world)}))
                 
  (handle-server-starting [this server]
    (post-event event-bus
                {:type :server-starting
                 :server server}))
                 
  (handle-server-stopping [this server] 
    (post-event event-bus
                {:type :server-stopping
                 :server server}))

  (handle-player-login [this player]
    (post-event event-bus
                {:type :player-login
                 :player player}))
                 
  (handle-player-logout [this player]
    (post-event event-bus  
                {:type :player-logout
                 :player player})))

;; Event handler registration
(defn register-forge-handlers! [mod-event-bus forge-event-bus bridge]
  ;; World events
  (.addListener forge-event-bus
    (reify Consumer
      (accept [this event]
        (when (instance? WorldEvent$Load event)
          (handle-world-load bridge (.getWorld event))))))
          
  (.addListener forge-event-bus  
    (reify Consumer  
      (accept [this event]
        (when (instance? WorldEvent$Unload event)
          (handle-world-unload bridge (.getWorld event))))))

  ;; Server events          
  (.addListener mod-event-bus
    (reify Consumer
      (accept [this event]
        (when (instance? ServerStartingEvent event)
          (handle-server-starting bridge (.getServer event))))))
          
  (.addListener mod-event-bus
    (reify Consumer  
      (accept [this event]
        (when (instance? ServerStoppingEvent event)
          (handle-server-stopping bridge (.getServer event))))))

  ;; Player events
  (.addListener forge-event-bus
    (reify Consumer  
      (accept [this event]
        (when (instance? PlayerEvent$PlayerLoggedInEvent event)
          (handle-player-login bridge (.getPlayer event))))))
          
  (.addListener forge-event-bus
    (reify Consumer
      (accept [this event]
        (when (instance? PlayerEvent$PlayerLoggedOutEvent event)
          (handle-player-logout bridge (.getPlayer event)))))))

;; Factory functions
(defn create-event-bus []
  (->ForgeEventBus (atom [])))
  
(defn create-event-bridge [event-bus]
  (->ForgeEventBridge event-bus))