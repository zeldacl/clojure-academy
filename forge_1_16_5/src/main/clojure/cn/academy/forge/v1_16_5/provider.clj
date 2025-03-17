(ns cn.academy.forge.v1_16_5.provider
  (:require [cn.academy.registry :as registry]
            [cn.academy.core :as core])
  (:import [net.minecraft.block Block Block$Properties]
           [net.minecraft.block.material Material]
           [net.minecraft.item Item Item$Properties BlockItem]
           [net.minecraftforge.registries IForgeRegistry ForgeRegistries]
           [net.minecraftforge.fml RegistryObject]
           [net.minecraftforge.fml.javafmlmod FMLJavaModLoadingContext]
           [net.minecraftforge.registries DeferredRegister]))

;; Create deferred registers for blocks and items
(def blocks-register 
  (DeferredRegister/create ForgeRegistries/BLOCKS core/modid))

(def items-register 
  (DeferredRegister/create ForgeRegistries/ITEMS core/modid))

(deftype Forge116Provider []
  registry/RegistryProvider
  
  (register-block [_ block-id block-constructor]
    (let [props (block-constructor)
          block (.register blocks-register 
                         block-id
                         (reify java.util.function.Supplier
                           (get [_]
                             (Block. (-> (Block$Properties/create (:material props Material/ROCK))
                                       (.hardnessAndResistance (:hardness props 3.0))
                                       (.lightValue (:light-level props 0)))))))]
      block))
  
  (register-item [_ item-id item-constructor]
    (let [props (item-constructor)
          item (.register items-register
                        item-id
                        (reify java.util.function.Supplier
                          (get [_]
                            (Item. (-> (Item$Properties.)
                                     (.maxStackSize (:max-stack-size props 64))
                                     (.maxDamage (:max-damage props 0))
                                     (.group core/creative-tab))))))]
      item))
  
  (register-tile-entity [_ te-id te-class]
    ;; TileEntity registration will be implemented here
    nil))

(defn init []
  (let [mod-bus (.getModEventBus (FMLJavaModLoadingContext/get))]
    (.register blocks-register mod-bus)
    (.register items-register mod-bus)
    (registry/init-registrations (Forge116Provider.))))