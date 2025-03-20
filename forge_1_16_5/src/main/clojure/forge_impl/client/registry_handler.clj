(ns forge-impl.client.registry-handler
  (:require [forge-impl.client.block.node-renderer :as node-renderer]
            [forge-impl.gui.gui-registry :as gui-registry]
            [clojure.tools.logging :as log])
  (:import [net.minecraftforge.fml.client.registry ClientRegistry]
           [net.minecraftforge.api.distmarker Dist OnlyIn]
           [net.minecraft.client.renderer.texture TextureAtlasSprite]
           [net.minecraft.client.renderer.model ModelBakery]
           [net.minecraft.util ResourceLocation]
           [net.minecraftforge.client.event TextureStitchEvent$Pre]
           [net.minecraftforge.eventbus.api SubscribeEvent]))

;; Node textures to register
(def node-textures
  ["textures/blocks/node_basic_off.png"
   "textures/blocks/node_basic_on_0.png"
   "textures/blocks/node_basic_on_1.png"
   "textures/blocks/node_basic_on_2.png"
   "textures/blocks/node_basic_on_3.png"
   "textures/blocks/node_basic_on_4.png"
   "textures/blocks/node_standard_off.png"
   "textures/blocks/node_standard_on_0.png"
   "textures/blocks/node_standard_on_1.png"
   "textures/blocks/node_standard_on_2.png"
   "textures/blocks/node_standard_on_3.png"
   "textures/blocks/node_standard_on_4.png"
   "textures/blocks/node_advanced_off.png"
   "textures/blocks/node_advanced_on_0.png"
   "textures/blocks/node_advanced_on_1.png"
   "textures/blocks/node_advanced_on_2.png"
   "textures/blocks/node_advanced_on_3.png"
   "textures/blocks/node_advanced_on_4.png"])

;; Handle texture stitching
(defn handle-texture-stitch! [^TextureStitchEvent$Pre event]
  (let [atlas (.getMap event)]
    (doseq [texture node-textures]
      (.addSprite atlas (ResourceLocation. "acmod" texture)))))

;; Register tile entity renderers
(defn register-tile-renderers! []
  (ClientRegistry/bindTileEntityRenderer
    forge-impl.registry.tile-entity-types/NODE_TILE_TYPE
    (node-renderer/create-renderer-factory)))

;; Register client-side handlers
(defn register-handlers! []
  (let [mod-bus (.. FMLJavaModLoadingContext get getModEventBus)]
    (.addListener mod-bus handle-texture-stitch!)))

;; Initialize client-side components
(defn init-client! []
  (log/info "Initializing client-side components")
  (register-tile-renderers!)
  (register-handlers!)
  (log/info "Client-side initialization complete"))