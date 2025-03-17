(ns cn.academy.block.matrix-forge-registry
  (:require [cn.academy.block.matrix-registry :as registry]
            [cn.academy.block.matrix-forge-events :as events]
            [cn.academy.block.matrix-nbt-adapter :as nbt])
  (:import [net.minecraftforge.fml.common.registry GameRegistry]
           [net.minecraftforge.registries ForgeRegistries]
           [net.minecraftforge.event RegistryEvent$Register]
           [net.minecraftforge.fml.event.lifecycle FMLClientSetupEvent]
           [net.minecraft.block Block]
           [net.minecraft.item Item]))

(defprotocol IForgeMatrixRegistry
  (register-block [this registry-event])
  (register-tile-entity [this])
  (register-item [this block registry-event])
  (register-renderer [this setup-event])
  (register-events [this]))

(defrecord ForgeMatrixRegistry [mod-id registry components]
  IForgeMatrixRegistry
  (register-block [_ block-event]
    (let [block (-> (:matrix components)
                    (assoc :material Material/IRON))]
      (-> block-event
          .getRegistry
          (.register (doto block
                      (.setRegistryName (str mod-id ":matrix")))))))
  
  (register-tile-entity [_]
    (let [tile-type (-> (:matrix components)
                        (create-tile-type))]
      (GameRegistry/registerTileEntity
        tile-type
        (ResourceLocation. mod-id "matrix_tile"))))
  
  (register-item [_ block item-event]
    (-> item-event
        .getRegistry
        (.register (doto (BlockItem. block {})
                    (.setRegistryName (.getRegistryName block))))))
  
  (register-renderer [_ setup-event]
    (let [gui-handler (get-in components [:gui])]
      (.enqueueWork setup-event
                    #(ClientRegistry/bindTileEntityRenderer
                       (get-in components [:tile-type])
                       (fn [] (create-renderer gui-handler))))))
  
  (register-events [_]
    (let [event-dispatcher (get-in components [:events])]
      (events/register-event-handlers event-dispatcher))))

(defn register-matrix [mod-id]
  (let [registry (registry/create-registry (config/create-matrix-config))
        components (registry/register-all registry)]
    (->ForgeMatrixRegistry mod-id registry components)))