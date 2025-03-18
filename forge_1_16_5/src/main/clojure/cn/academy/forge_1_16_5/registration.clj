(ns cn.academy.forge-1-16-5.registration
  (:require [cn.academy.mod :as mod]
            [cn.academy.block.registration-impl :as block-reg])
  (:import [net.minecraftforge.registries ForgeRegistries DeferredRegister]
           [net.minecraftforge.fml.javafmlmod FMLJavaModLoadingContext]
           [net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent FMLClientSetupEvent]
           [net.minecraftforge.eventbus.api IEventBus]))

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
  
  (register-deferred! [_ mod-bus]
    (doseq [registry-type [ForgeRegistries/BLOCKS 
                          ForgeRegistries/ITEMS 
                          ForgeRegistries/TILE_ENTITIES]]
      (-> (DeferredRegister/create registry-type mod/MOD-ID)
          (.register ^IEventBus mod-bus)))))

(defn create-registration []
  (->ModRegistration))

(defn create-handler []
  (->RegistrationHandler))