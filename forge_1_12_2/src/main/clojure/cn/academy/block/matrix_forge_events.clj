(ns cn.academy.block.matrix-forge-events
  (:require [cn.academy.block.matrix-events :as events])
  (:import [net.minecraftforge.event.world.BlockEvent$BreakEvent]
           [net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickBlock]
           [net.minecraftforge.fml.common.eventhandler.SubscribeEvent]
           [net.minecraftforge.fml.common.Mod$EventHandler]))

(defrecord ForgeEventHandler [dispatcher]
  Object
  (^SubscribeEvent onBlockBreak [_ ^BlockEvent$BreakEvent event]
    (let [world (.world event)
          pos (.getPos event)]
      (events/on-block-break dispatcher world pos)))
  
  (^SubscribeEvent onBlockActivate [_ ^PlayerInteractEvent$RightClickBlock event]
    (let [world (.world event)
          pos (.getPos event)
          player (.getEntityPlayer event)
          hand (.getHand event)
          face (.getFace event)
          hit-vec (.getHitVec event)]
      (when-let [result (events/on-block-activate dispatcher 
                                                 world 
                                                 pos 
                                                 player 
                                                 hand 
                                                 face 
                                                 hit-vec)]
        (when (= (:type result) :open-gui)
          (.setCanceled event true))))))

(defn register-event-handlers [dispatcher]
  (let [handler (->ForgeEventHandler dispatcher)]
    (MinecraftForge/EVENT_BUS.register handler)))