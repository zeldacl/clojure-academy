(ns cn.academy.forge.v1_12_2.provider
  (:require [cn.academy.registry :as registry]
            [cn.academy.core :as core]
            [clojure.tools.logging :as log])
  (:import [net.minecraft.block Block]
           [net.minecraft.block.material Material]
           [net.minecraft.item Item ItemBlock]
           [net.minecraftforge.event RegistryEvent$Register]
           [net.minecraftforge.fml.common.registry GameRegistry]
           [net.minecraftforge.registries IForgeRegistry]))

;; Atoms to store registration callbacks for the events
(def block-registrations (atom []))
(def item-registrations (atom []))
(def tile-registrations (atom []))

(deftype Forge112Provider []
  registry/RegistryProvider
  
  (register-block [_ block-id block-constructor]
    (swap! block-registrations conj 
           (fn [^RegistryEvent$Register event]
             (let [props (block-constructor)
                   block (proxy [Block] [(:material props Material/ROCK)]
                          (getHarvestLevel [state] 
                            (:harvest-level props 0)))]
               (doto block
                 (.setHardness (:hardness props 3.0))
                 (.setResistance (:resistance props 5.0))
                 (.setLightLevel (:light-level props 0))
                 (.setCreativeTab core/creative-tab)
                 (.setRegistryName core/modid block-id)
                 (.setTranslationKey (str "ac_" block-id)))
               (.register (.getRegistry event) block)))))
  
  (register-item [_ item-id item-constructor]
    (swap! item-registrations conj
           (fn [^RegistryEvent$Register event]
             (let [props (item-constructor)
                   item (proxy [Item] []
                         (getItemStackLimit [] 
                           (:max-stack-size props 64))
                         (getMaxDamage [stack]
                           (:max-damage props 0)))]
               (doto item
                 (.setCreativeTab core/creative-tab)
                 (.setRegistryName core/modid item-id)
                 (.setTranslationKey (str "ac_" item-id)))
               (.register (.getRegistry event) item)))))
  
  (register-tile-entity [_ te-id te-class]
    (swap! tile-registrations conj
           #(GameRegistry/registerTileEntity 
              te-class 
              (str core/modid ":" te-id)))))

(defn handle-block-registry [^RegistryEvent$Register event]
  (doseq [register-fn @block-registrations]
    (register-fn event)))

(defn handle-item-registry [^RegistryEvent$Register event]
  (doseq [register-fn @item-registrations]
    (register-fn event)))

(defn init []
  (let [provider (Forge112Provider.)]
    (registry/init-registrations provider)
    ;; Tile entities are registered immediately in 1.12.2
    (doseq [register-fn @tile-registrations]
      (register-fn))))