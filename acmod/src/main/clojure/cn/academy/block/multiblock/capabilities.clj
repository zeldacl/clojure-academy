(ns cn.academy.block.multiblock.capabilities
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [mcmod.capability :as cap]
            [mcmod.energy :as energy]
            [mcmod.fluid :as fluid]
            [mcmod.item :as item]))

(defprotocol ICapabilityProvider
  "Protocol for providing capabilities"
  (get-capability [this type] "Get capability of given type")
  (has-capability? [this type] "Check if has capability"))

(defrecord MultiblockCapabilityProvider [multiblock caps]
  ICapabilityProvider
  (get-capability [_ type]
    (when-let [controller (base/get-controller multiblock)]
      (when-let [cap-instance (get caps type)]
        (cap/wrap-capability cap-instance controller))))
  
  (has-capability? [_ type]
    (contains? caps type)))

(defrecord MultiblockEnergyHandler [multiblock max-transfer]
  energy/IEnergyStorage
  (receive-energy [_ amount simulate]
    (when-let [controller (base/get-controller multiblock)]
      (let [space (- (energy/get-max-energy controller)
                     (energy/get-stored-energy controller))
            transfer (min amount max-transfer space)]
        (when-not simulate
          (energy/add-energy! controller transfer))
        transfer)))
  
  (extract-energy [_ amount simulate]
    (when-let [controller (base/get-controller multiblock)]
      (let [stored (energy/get-stored-energy controller)
            transfer (min amount max-transfer stored)]
        (when-not simulate
          (energy/extract-energy! controller transfer))
        transfer)))
  
  (get-energy-stored [_]
    (when-let [controller (base/get-controller multiblock)]
      (energy/get-stored-energy controller)))
  
  (get-max-energy-stored [_]
    (when-let [controller (base/get-controller multiblock)]
      (energy/get-max-energy controller)))
  
  (can-receive [_] true)
  (can-extract [_] true))

(defrecord MultiblockFluidHandler [multiblock tank-id]
  fluid/IFluidHandler
  (fill [_ fluid amount simulate]
    (when-let [controller (base/get-controller multiblock)]
      (fluid/fill controller tank-id fluid amount simulate)))
  
  (drain [_ fluid amount simulate]
    (when-let [controller (base/get-controller multiblock)]
      (fluid/drain controller tank-id fluid amount simulate)))
  
  (get-fluid-in-tank [_ tank]
    (when-let [controller (base/get-controller multiblock)]
      (fluid/get-fluid-in-tank controller tank)))
  
  (get-tank-capacity [_ tank]
    (when-let [controller (base/get-controller multiblock)]
      (fluid/get-tank-capacity controller tank)))
      
  (get-tanks [_]
    (when-let [controller (base/get-controller multiblock)]
      (fluid/get-tanks controller))))

(defrecord MultiblockItemHandler [multiblock inventory-id]
  item/IItemHandler
  (get-slots [_]
    (when-let [controller (base/get-controller multiblock)]
      (item/get-slots controller)))
  
  (get-stack-in-slot [_ slot]
    (when-let [controller (base/get-controller multiblock)]
      (item/get-stack-in-slot controller slot)))
  
  (insert-item [_ slot item-stack simulate]
    (when-let [controller (base/get-controller multiblock)]
      (item/insert-item controller slot item-stack simulate)))
  
  (extract-item [_ slot amount simulate]
    (when-let [controller (base/get-controller multiblock)]
      (item/extract-item controller slot amount simulate)))
  
  (get-slot-limit [_ slot]
    (when-let [controller (base/get-controller multiblock)]
      (item/get-slot-limit controller slot))))

(defn create-capability-provider
  "Create a new multiblock capability provider"
  [multiblock capabilities]
  (->MultiblockCapabilityProvider multiblock capabilities))

(defn create-energy-handler
  "Create a new multiblock energy handler"
  [multiblock max-transfer]
  (->MultiblockEnergyHandler multiblock max-transfer))

(defn create-fluid-handler
  "Create a new multiblock fluid handler" 
  [multiblock tank-id]
  (->MultiblockFluidHandler multiblock tank-id))

(defn create-item-handler
  "Create a new multiblock item handler"
  [multiblock inventory-id]
  (->MultiblockItemHandler multiblock inventory-id))