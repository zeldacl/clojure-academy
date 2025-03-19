(ns cn.academy.forge.v1_16_5.provider
  (:require [cn.academy.registry :as registry]
            [cn.academy.core :as core]
            [cn.academy.forge.v1_16_5.bridge.block :as block-bridge]
            [cn.academy.forge.v1_16_5.bridge.registry :as registry-bridge])
  (:import [net.minecraft.block Block Block$Properties]
           [net.minecraft.block.material Material]
           [net.minecraft.item Item Item$Properties BlockItem]
           [net.minecraftforge.registries IForgeRegistry ForgeRegistries]
           [net.minecraftforge.fml RegistryObject]
           [net.minecraftforge.fml.javafmlmod FMLJavaModLoadingContext]
           [net.minecraftforge.registries DeferredRegister]))

(deftype Forge116Provider []
  registry/RegistryProvider
  
  (register-block [_ block-id block-constructor]
    (let [props (block-constructor)
          block-factory #(let [block-bridge (block-bridge/create-bridge)]
                           (.create-block block-bridge props))]
      (registry-bridge/register-block! block-id props)
      (-> (registry-bridge/create-block-with-item 
            block-id 
            (Item$Properties.) 
            block-factory)
          :block)))
  
  (register-item [_ item-id item-constructor]
    (let [props (item-constructor)
          item-factory #(Item. (-> (Item$Properties.)
                                (.maxStackSize (:max-stack-size props 64))
                                (.maxDamage (:max-damage props 0))
                                (.group core/creative-tab)))]
      (registry-bridge/register-item! item-id props)
      (.register registry-bridge/items-registry
                item-id
                (reify java.util.function.Supplier
                  (get [_] (item-factory))))))
  
  (register-tile-entity [_ te-id te-class]
    (let [factory #(te-class)
          reg-obj (.register registry-bridge/tile-entities-registry
                           te-id
                           (reify java.util.function.Supplier
                             (get [_] (factory))))]
      (registry-bridge/register-tile-entity! te-id factory)
      reg-obj)))

(defn init []
  (registry/init-registrations (->Forge116Provider)))