(ns cn.academy.energy.client.renderer
  (:require [cn.academy.energy.client.range-visualizer :as range-viz]
            [cn.academy.energy.api.wireless :as wireless])
  (:import [net.minecraftforge.api.distmarker Dist]
           [net.minecraftforge.client.event RenderWorldLastEvent]
           [net.minecraftforge.eventbus.api SubscribeEvent]
           [net.minecraftforge.fml.common Mod$EventBusSubscriber]))

(gen-class
  :name cn.academy.energy.client.WirelessRenderer
  :extends java.lang.Object
  :prefix "renderer-"
  :implements [net.minecraftforge.eventbus.api.IEventBus]
  :state state)

(defn renderer-init []
  (proxy [Mod$EventBusSubscriber] [[Dist/CLIENT]])
  
  @SubscribeEvent
  (defn on-render-world [^RenderWorldLastEvent event]
    (let [world (.getWorld *minecraft*)
          player (.getPlayer *minecraft*)]
      ; Render range indicators for nodes and matrices in view
      (doseq [entity (.loadedTileEntityList world)
              :when (or (wireless/is-wireless-node? entity)
                       (wireless/is-wireless-matrix? entity))]
        (cond
          (wireless/is-wireless-node? entity)
          (range-viz/render-node-range entity)
          
          (wireless/is-wireless-matrix? entity) 
          (range-viz/render-matrix-range entity)))))))