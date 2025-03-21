(ns cn.lambdalib2.core
  (:require [clojure.java.io :as io]
            [mcmod.mod :as mod]
            [mcmod.event :as event]
            [mcmod.network :as network]
            [mcmod.logging :as log]))

(def modid "lambdalib2")
(def version "1.0.0")
(def debug true)
(def channel (network/create-channel modid))
(def logger (atom nil))

(defn get-logger [] @logger)

(defn pre-init [event]
  (reset! logger (log/get-mod-logger event))
  (network/register-message channel :network-event-client network/NetworkEvent$MessageHandler network/NetworkEvent$Message :client)
  (network/register-message channel :network-event-server network/NetworkEvent$MessageHandler network/NetworkEvent$Message :server)
  (network/register-message channel :network-message-client network/NetworkMessage$Handler network/NetworkMessage$Message :client)
  (network/register-message channel :network-message-server network/NetworkMessage$Handler network/NetworkMessage$Message :server)
  (network/register-message channel :block-multi-req network/MsgBlockMulti$ReqHandler network/MsgBlockMulti$Req :server)
  (network/register-message channel :block-multi network/MsgBlockMulti$Handler network/MsgBlockMulti :client)
  (mod/handle-registration-event this event))

(defn init [event]
  (when debug (log/info @logger "LambdaLib2 is running in development mode."))
  (mod/handle-registration-event this event))

(defn init-client [event]
  (event/register-handler (mod/create-debug-draw)))

(defn post-init [event]
  (mod/handle-registration-event this event))

(defn load-complete [event]
  (mod/handle-registration-event this event))

(defn server-stopped [event]
  (mod/handle-registration-event this event))

(defn server-started [event]
  (mod/handle-registration-event this event))

(defn server-stopping [event]
  (mod/handle-registration-event this event))

(defn server-starting [event]
  (mod/handle-registration-event this event))

(defn server-about-to-start [event]
  (mod/handle-registration-event this event))

;; Register the mod and its event handlers using the mcmod abstraction
(mod/register-mod 
  {:modid modid 
   :version version
   :event-handlers {:pre-init pre-init
                   :init init
                   :init-client init-client
                   :post-init post-init
                   :load-complete load-complete
                   :server-stopped server-stopped
                   :server-started server-started
                   :server-stopping server-stopping
                   :server-starting server-starting
                   :server-about-to-start server-about-to-start}})
