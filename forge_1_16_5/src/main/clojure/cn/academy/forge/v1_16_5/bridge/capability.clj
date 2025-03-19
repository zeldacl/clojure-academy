(ns cn.academy.forge.v1_16_5.bridge.capability
  (:require [cn.academy.capability.core :as cap]
            [cn.academy.block.component :as component]
            [cn.academy.forge.v1_16_5.bridge.energy :as energy-bridge]
            [cn.academy.forge.v1_16_5.bridge.inventory :as inv-bridge]
            [cn.academy.forge.v1_16_5.bridge.nbt :as nbt])
  (:import [net.minecraftforge.common.capabilities ICapabilityProvider Capability]
           [net.minecraftforge.items CapabilityItemHandler]
           [net.minecraftforge.energy CapabilityEnergy]
           [net.minecraftforge.common.capabilities CapabilityInject ICapabilitySerializable]
           [net.minecraftforge.common.util LazyOptional]
           [net.minecraft.util Direction]
           [net.minecraft.nbt CompoundNBT]
           [javax.annotation Nullable]))

(defn- create-handler [capability provider energy-bridge inv-bridge]
  (LazyOptional/of 
    (fn []
      (case (cap/get-type-id capability)
        :energy (energy-bridge/to-forge-energy 
                  energy-bridge 
                  (cap/get-capability provider :energy nil))
        :inventory (inv-bridge/to-forge-item-handler 
                    inv-bridge
                    (cap/get-capability provider :inventory nil))
        nil))))

(defrecord ForgeCapabilityAdapter [component capabilities]
  Object
  (getCapability [this cap side]
    (if-let [capability (get @capabilities cap)]
      capability
      (let [comp-cap (component/get-capability component cap side)]
        (if comp-cap
          (let [lazy-opt (LazyOptional/of (fn [] comp-cap))] 
            (swap! capabilities assoc cap lazy-opt)
            lazy-opt)
          LazyOptional/EMPTY)))))

;; Creates a new capability adapter for the given component
(defn create-adapter [component]
  (->ForgeCapabilityAdapter component (atom {})))

;; Invalidates all capabilities stored in the adapter
(defn invalidate-capabilities! [adapter]
  (doseq [[_ lazy-opt] @(:capabilities adapter)]
    (.invalidate lazy-opt))
  (reset! (:capabilities adapter) {}))

;; This adapter bridges between our component system and Forge's capability system
(defn create-capability-provider [component]
  (reify 
    ICapabilitySerializable
    (serializeNBT [_] 
      (let [tag (CompoundNBT.)]
        (when (satisfies? component/NBTSerializable component)
          (let [data (.serialize component)]
            (nbt/write-nbt tag data)))
        tag))
    
    (deserializeNBT [_ ^CompoundNBT nbt]
      (when (satisfies? component/NBTSerializable component)
        (let [data (nbt/read-nbt nbt)]
          (.deserialize component data))))
    
    ICapabilityProvider
    (getCapability [_ ^Capability cap ^Nullable dir]
      (if (and component (= cap (get-capability-instance (type component))))
        (LazyOptional/of #(cast (get-capability-class (type component)) component))
        LazyOptional/EMPTY))))

;; Maintain a registry of capability classes and instances for our components
(def capability-registry (atom {}))

(defn register-capability [component-type cap-class cap-instance]
  (swap! capability-registry assoc component-type 
         {:class cap-class :instance cap-instance}))

(defn get-capability-class [component-type]
  (get-in @capability-registry [component-type :class]))

(defn get-capability-instance [component-type]
  (get-in @capability-registry [component-type :instance]))

;; Macro to inject capability and register it for a component type
(defmacro define-capability [component-type cap-var]
  `(do
     (def ~cap-var nil)
     (CapabilityInject ~(get-capability-class component-type))
     (set! ~cap-var (get-capability-instance component-type))))