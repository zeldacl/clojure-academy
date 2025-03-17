(ns cn.academy.block.matrix-forge-registry
  (:require [cn.academy.block.matrix-registry :as registry]
            [cn.academy.block.matrix-forge-events :as events]
            [cn.academy.block.matrix-nbt-adapter :as nbt])
  (:import [net.minecraftforge.fml.common.registry GameRegistry]
           [net.minecraftforge.registries ForgeRegistries]
           [net.minecraft.block Block]
           [net.minecraft.item Item]))

(defprotocol IForgeMatrixRegistry
  (register-block [this])
  (register-tile-entity [this])
  (register-item [this block])
  (register-renderer [this])
  (register-events [this]))

(defrecord ForgeMatrixRegistry [mod-id registry components]
  IForgeMatrixRegistry
  (register-block [_]
    (let [block (-> (:matrix components)
                    (assoc :material Material/IRON))]
      (.register ForgeRegistries/BLOCKS
                (doto block
                  (.setRegistryName (str mod-id ":matrix"))))))
  
  (register-tile-entity [_]
    (let [tile-type (-> (:matrix components)
                        (create-tile-type))]
      (.register ForgeRegistries/TILE_ENTITIES
                tile-type
                (ResourceLocation. mod-id "matrix_tile"))))
  
  (register-item [_ block]
    (.register ForgeRegistries/ITEMS
              (doto (BlockItem. block {})
                (.setRegistryName (.getRegistryName block)))))
  
  (register-renderer [_]
    (let [gui-handler (get-in components [:gui])]
      (ClientRegistry/registerTileEntityRenderer
        (get-in components [:tile-type])
        (fn [] (create-renderer gui-handler)))))
  
  (register-events [_]
    (let [event-dispatcher (get-in components [:events])]
      (events/register-event-handlers event-dispatcher))))

(defn register-matrix [mod-id]
  (let [registry (registry/create-registry (config/create-matrix-config))
        components (registry/register-all registry)
        forge-registry (->ForgeMatrixRegistry mod-id registry components)]
    (doto forge-registry
      (register-block)
      (register-tile-entity)
      (register-renderer)
      (register-events))))