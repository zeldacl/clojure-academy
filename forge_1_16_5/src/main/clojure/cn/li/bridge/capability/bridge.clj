(ns cn.li.bridge.capability.bridge
  (:require [cn.li.bridge.capability.api :as cap])
  (:import [net.minecraftforge.common.capabilities Capability ICapabilityProvider CapabilityManager]
           [net.minecraft.util Direction]
           [net.minecraftforge.common.util LazyOptional]))

(defprotocol ICapabilityBridge
  "Bridge between platform-independent capabilities and Forge"
  (register-capability [this capability-class]
    "Register a new capability type")
  (create-provider [this capabilities]
    "Create a capability provider")
  (get-capability [this provider cap side]
    "Get capability from provider"))

(defrecord ForgeCapabilityProvider [capabilities]
  ICapabilityProvider
  (getCapability [_ cap side]
    (if-let [capability (get @capabilities [cap side])]
      (LazyOptional/of (constantly capability))
      LazyOptional/EMPTY)))

(defrecord ForgeCapabilityBridge []
  ICapabilityBridge
  (register-capability [_ capability-class]
    (CapabilityManager/INSTANCE.register capability-class))
  
  (create-provider [_ capabilities]
    (->ForgeCapabilityProvider (atom capabilities)))
  
  (get-capability [_ provider cap side]
    (.getCapability provider cap (when side (.get side)))))

(defn create-capability-bridge []
  (->ForgeCapabilityBridge))

(defn initialize-capabilities []
  (let [bridge (create-capability-bridge)]
    (cap/register-core-capabilities bridge)))