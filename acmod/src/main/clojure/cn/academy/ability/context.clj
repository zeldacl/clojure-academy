(ns cn.academy.ability.context
  (:require [clojure.tools.logging :as log])
  (:import [net.minecraft.client Minecraft]
           [net.minecraft.entity.player EntityPlayer]
           [net.minecraft.world World]
           [net.minecraftforge.fml.relauncher SideOnly]
           [cn.academy.ability AbilityContext Skill]
           [cn.lambdalib2.s11n.network NetworkMessage]
           [cn.lambdalib2.util Debug]))

(def debug-msg false)

(defn- message-debug [s]
  (when (and debug debug-msg)
    (Debug/log (str "[Context]" s))))

(defn- is-remote [player]
  (.isRemote (.world player)))

(defn- is-local [player]
  (if (is-remote player)
    (= (.player (Minecraft/getMinecraft)) player)
    false))

(defn- send-message [mgr context channel args]
  (message-debug (str "ToServer: " channel))
  (.mToServer mgr context channel args))

(defn create-context [player skill]
  {:player player
   :skill skill
   :ctx (AbilityContext/of player skill)
   :status :constructed
   :server-id 0
   :client-contexts (when (is-remote player) (construct-client-contexts player skill))})

(defn construct-client-contexts [player skill]
  (map #(% {:player player :skill skill}) (ClientContext/clientTypes (class skill))))

(defn terminate [context]
  (ContextManager/instance.terminate context))

(defn send-to-server [context channel & args]
  (send-message (ContextManager/instance) context channel args))

(defn send-to-client [context channel & args]
  (send-message (ContextManager/instance) context channel args))

(defn send-to-local [context channel & args]
  (send-message (ContextManager/instance) context channel args))

(defn send-to-except-local [context channel & args]
  (send-message (ContextManager/instance) context channel args))

(defn send-to-self [context channel & args]
  (send-message (ContextManager/instance) context channel args))

(defn debug [msg]
  (log/info (str "[CTX]" msg)))