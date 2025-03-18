(ns cn.academy.forge-1-12-2.registration
  (:require [cn.academy.mod :as mod]
            [cn.academy.block.registration-impl :as block-reg])
  (:import [net.minecraftforge.fml.common.registry GameRegistry]
           [net.minecraftforge.fml.common.event FMLPreInitializationEvent]
           [net.minecraftforge.event RegistryEvent$Register]
           [net.minecraftforge.fml.common.eventhandler SubscribeEvent]
           [net.minecraft.block Block]
           [net.minecraft.item Item]
           [net.minecraft.tileentity TileEntity]
           [net.minecraft.util ResourceLocation]))

(defrecord ModRegistration []
  mod/IModSetup
  (setup-common! [_]
    ;; Common initialization logic
    nil)
  
  (setup-client! [_]
    ;; Client-side initialization logic
    nil)
  
  (register-all! [_]
    (let [registration (block-reg/create-registration)]
      (mod/init-mod! registration))))

(defrecord RegistrationHandler []
  mod/IRegistrationHandler
  (create-registration [_]
    (block-reg/create-registration))
  
  ;; In 1.12.2 we don't have deferred registration, instead we use event handlers
  (register-deferred! [_ _]
    ;; Register event handlers that will handle block/item registration
    (GameRegistry/register this)))

;; Event handlers for 1.12.2 registration
(defprotocol IRegistryEvents
  (^SubscribeEvent on-block-registry [this ^RegistryEvent$Register event])
  (^SubscribeEvent on-item-registry [this ^RegistryEvent$Register event])
  (register-tile-entities [this]))

(extend-type RegistrationHandler
  IRegistryEvents
  (on-block-registry [_ event]
    (let [registration (block-reg/create-registration)]
      (mod/init-mod! registration)))
  
  (on-item-registry [_ event]
    ;; Item registration will be handled by the block registration since we're using ItemBlocks)
    nil)
  
  (register-tile-entities [_]
    ;; TileEntity registration is done after block registration in 1.12.2
    nil))

(defn create-registration []
  (->ModRegistration))

(defn create-handler []
  (->RegistrationHandler))