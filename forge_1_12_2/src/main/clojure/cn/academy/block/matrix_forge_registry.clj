(ns cn.academy.block.matrix-forge-registry
  (:require [cn.academy.block.matrix-registry :as registry]
            [cn.academy.block.matrix-forge-events :as events]
            [cn.academy.block.matrix-nbt-adapter :as nbt])
  (:import [net.minecraftforge.fml.common.registry GameRegistry]
           [net.minecraftforge.client.model ModelLoader]
           [net.minecraft.block Block]
           [net.minecraft.item Item ItemBlock]))

(defprotocol IForgeMatrixRegistry
  (register-block [this])
  (register-tile-entity [this])
  (register-item [this block])
  (register-models [this])
  (register-events [this]))

(defrecord ForgeMatrixRegistry [mod-id registry components]
  IForgeMatrixRegistry
  (register-block [_]
    (let [block (-> (:matrix components)
                    (assoc :material Material/IRON)
                    (doto (.setUnlocalizedName (str mod-id ".matrix"))))]
      (GameRegistry/register block (ResourceLocation. mod-id "matrix"))
      block))
  
  (register-tile-entity [_]
    (let [tile-type (-> (:matrix components)
                        (create-tile-type))]
      (GameRegistry/registerTileEntity
        tile-type
        (ResourceLocation. mod-id "matrix_tile"))))
  
  (register-item [_ block]
    (let [item (ItemBlock. block)]
      (GameRegistry/register item (.getRegistryName block))))
  
  (register-models [_]
    (let [item (get-in components [:item])
          model-loc (ResourceLocation. mod-id "matrix")]
      (ModelLoader/setCustomModelResourceLocation
        item 0 
        (ModelResourceLocation. model-loc "inventory"))))
  
  (register-events [_]
    (let [event-dispatcher (get-in components [:events])]
      (events/register-event-handlers event-dispatcher))))

(defn register-matrix-preinit [mod-id]
  (let [registry (registry/create-registry (config/create-matrix-config))
        components (registry/register-all registry)
        forge-registry (->ForgeMatrixRegistry mod-id registry components)]
    (doto forge-registry
      register-block
      register-tile-entity)))

(defn register-matrix-init [registry]
  (doto registry
    register-events))

(defn register-matrix-client-init [registry]
  (doto registry
    register-models))