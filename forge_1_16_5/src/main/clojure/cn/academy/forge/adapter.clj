(ns cn.academy.forge.adapter
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core])
  (:import [net.minecraft.block Block BlockState]
           [net.minecraft.tileentity TileEntity]
           [net.minecraft.item Item ItemStack]
           [net.minecraft.util Direction]
           [net.minecraftforge.common capabilities Capability CapabilityInject]
           [net.minecraftforge.energy IEnergyStorage]
           [net.minecraftforge.items IItemHandler]))

;; Create Forge capability wrappers
(defrecord ForgeEnergyAdapter [storage]
  IEnergyStorage
  (receiveEnergy [this maxReceive simulate]
    (receive-energy storage maxReceive simulate))
  
  (extractEnergy [this maxExtract simulate]  
    (extract-energy storage maxExtract simulate))
    
  (getEnergyStored [this]
    (get-energy-stored storage))
    
  (getMaxEnergyStored [this]
    (get-max-energy-stored storage))
    
  (canExtract [this]
    (can-extract? storage))
    
  (canReceive [this]
    (can-receive? storage)))

(defrecord ForgeItemHandlerAdapter [handler]
  IItemHandler
  (getSlots [this]
    (get-slots handler))
    
  (getStackInSlot [this slot]
    (get-stack-in-slot handler slot))
    
  (insertItem [this slot stack simulate]
    (insert-item handler slot stack simulate))
    
  (extractItem [this slot amount simulate]
    (extract-item handler slot amount simulate))
    
  (getSlotLimit [this slot]
    (get-slot-limit handler slot))
    
  (isItemValid [this slot stack]  
    (is-item-valid? handler slot stack)))

;; Create registry bridge implementation  
(defrecord ForgeRegistryBridge [mod-id]
  IRegistryBridge
  (register-block [this id block]
    (let [forge-block (proxy [Block] [(Block$Properties/create Material/IRON)]
                       (getStateForPlacement [ctx]
                         (on-placed block ctx))
                       (onBlockActivated [state world pos player hand]
                         (on-activated block state world pos player hand)))]
      (.setRegistryName forge-block (str mod-id ":" id))
      forge-block))

  (register-item [this id item]
    (let [forge-item (proxy [Item] [(Item$Properties.)]
                      (getMaxStackSize [] 
                        (get-max-stack-size item))
                      (isDamageable []
                        (> (get-max-damage item) 0)))]
      (.setRegistryName forge-item (str mod-id ":" id))
      forge-item))
      
  (register-tile-entity [this id type]
    (let [te-type (proxy [TileEntityType] []
                    (create []
                      (type)))]
      (.setRegistryName te-type (str mod-id ":" id))
      te-type)))

;; Create capability bridge implementation
(defrecord ForgeCapabilityBridge []
  ICapabilityBridge
  (register-capability [this capability-class]
    (CapabilityInject/inject capability-class))
    
  (create-provider [this capabilities]
    (proxy [ICapabilityProvider] []
      (getCapability [^Capability cap ^Direction side]
        (when-let [cap-impl (get capabilities cap)]
          (cond
            (instance? IEnergyStorage cap)
            (->ForgeEnergyAdapter cap-impl)
            
            (instance? IItemHandler cap)  
            (->ForgeItemHandlerAdapter cap-impl)
            
            :else cap-impl)))))
            
  (get-capability [this provider cap side]
    (.getCapability provider cap side)))

;; Factory functions to create adapters
(defn create-registry-bridge [mod-id]
  (->ForgeRegistryBridge mod-id))
  
(defn create-capability-bridge []
  (->ForgeCapabilityBridge))

;; Register mod capabilities
(defn register-capabilities! [capability-bridge]
  (register-capability capability-bridge IEnergyStorage)
  (register-capability capability-bridge IItemHandler))