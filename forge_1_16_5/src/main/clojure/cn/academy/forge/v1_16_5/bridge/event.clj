(ns cn.academy.forge.v1_16_5.bridge.event
  (:require [cn.academy.event.core :as event])
  (:import [net.minecraftforge.common MinecraftForge]
           [net.minecraftforge.eventbus.api IEventBus EventPriority]
           [net.minecraftforge.event.entity.player PlayerEvent$PlayerLoggedInEvent]
           [net.minecraftforge.event.entity.player PlayerEvent$PlayerLoggedOutEvent]
           [net.minecraftforge.event.world WorldEvent$Load WorldEvent$Save]
           [net.minecraftforge.event.world WorldEvent$Unload]
           [net.minecraftforge.event.TickEvent$ServerTickEvent TickEvent$Phase]
           [net.minecraftforge.event.TickEvent$WorldTickEvent]
           [net.minecraft.world.server ServerWorld]))

(defprotocol IEventBridge
  "Bridge between platform-independent events and Forge"
  (register-handler [this event-type handler]
    "Register a handler for a specific event type")
  (unregister-handler [this event-type handler]
    "Unregister a handler for a specific event type")
  (dispatch-event [this event-data]
    "Dispatch an event to all relevant handlers"))

(deftype ForgeEventAdapter [handler]
  Object
  (onPlayerLogin [_ event]
    (handler {:type :player-login
              :player {:id (.getUUID (.getPlayer event))
                       :name (.getName (.getPlayer event))}}))
  
  (onPlayerLogout [_ event]
    (handler {:type :player-logout
              :player {:id (.getUUID (.getPlayer event))
                       :name (.getName (.getPlayer event))}}))
  
  (onWorldLoad [_ event]
    (handler {:type :world-load
              :world {:id (.dimension (.getWorld event))}}))
  
  (onWorldUnload [_ event]
    (handler {:type :world-unload
              :world {:id (.dimension (.getWorld event))}}))
  
  (onWorldSave [_ event]
    (handler {:type :world-save
              :world {:id (.dimension (.getWorld event))}}))
  
  (onServerTick [_ event]
    (when (= (.phase event) TickEvent$Phase/END)
      (handler {:type :server-tick})))
  
  (onWorldTick [_ event]
    (when (and (= (.phase event) TickEvent$Phase/END)
               (instance? ServerWorld (.world event)))
      (handler {:type :world-tick
                :world {:id (.dimension (.world event))}}))))

(deftype ForgeEventBridge [handlers event-bus]
  IEventBridge
  (register-handler [_ event-type handler]
    (let [adapter (ForgeEventAdapter. handler)]
      (swap! handlers assoc handler {:type event-type :adapter adapter})
      (case event-type
        :player-login (.addListener event-bus 
                                  (reify java.util.function.Consumer
                                    (accept [_ event]
                                      (.onPlayerLogin adapter event)))
                                  PlayerEvent$PlayerLoggedInEvent)
        
        :player-logout (.addListener event-bus
                                   (reify java.util.function.Consumer
                                     (accept [_ event]
                                       (.onPlayerLogout adapter event)))
                                   PlayerEvent$PlayerLoggedOutEvent)
        
        :world-load (.addListener event-bus
                                (reify java.util.function.Consumer
                                  (accept [_ event]
                                    (.onWorldLoad adapter event)))
                                WorldEvent$Load)
        
        :world-unload (.addListener event-bus
                                  (reify java.util.function.Consumer
                                    (accept [_ event]
                                      (.onWorldUnload adapter event)))
                                  WorldEvent$Unload)
        
        :world-save (.addListener event-bus
                                (reify java.util.function.Consumer
                                  (accept [_ event]
                                    (.onWorldSave adapter event)))
                                WorldEvent$Save)
        
        :server-tick (.addListener event-bus
                                 (reify java.util.function.Consumer
                                   (accept [_ event]
                                     (.onServerTick adapter event)))
                                 TickEvent$ServerTickEvent)
        
        :world-tick (.addListener event-bus
                                (reify java.util.function.Consumer
                                  (accept [_ event]
                                    (.onWorldTick adapter event)))
                                TickEvent$WorldTickEvent))))
  
  (unregister-handler [_ event-type handler]
    (when-let [{:keys [adapter]} (get @handlers handler)]
      (swap! handlers dissoc handler)))
  
  (dispatch-event [_ event-data]
    (event/dispatch-event event-data)))

;; Create a bridge for the Forge event bus
(defn create-forge-bridge []
  (->ForgeEventBridge (atom {}) MinecraftForge/EVENT_BUS))

;; Create a bridge for the mod event bus
(defn create-mod-bridge [mod-bus]
  (->ForgeEventBridge (atom {}) mod-bus))

;; Register an event handler that will receive platform-independent events
(defn register-event-handler [bridge event-type handler]
  (.register-handler bridge event-type handler))

;; Unregister a previously registered event handler
(defn unregister-event-handler [bridge event-type handler]
  (.unregister-handler bridge event-type handler))