(ns cn.academy.block.matrix-forge-events
  (:require [cn.academy.block.matrix-events :as events])
  (:import [net.minecraftforge.event.world.BlockEvent$BreakEvent]
           [net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickBlock]
           [net.minecraftforge.eventbus.api.SubscribeEvent]
           [net.minecraftforge.fml.common.Mod$EventBusSubscriber]
           [net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus]))

(defrecord ForgeEventHandler [dispatcher]
  Object
  (^SubscribeEvent onBlockBreak [_ ^BlockEvent$BreakEvent event]
    (let [world (.getWorld event)
          pos (.getPos event)]
      (events/on-block-break dispatcher world pos)))
  
  (^SubscribeEvent onBlockActivate [_ ^PlayerInteractEvent$RightClickBlock event]
    (let [world (.getWorld event)
          pos (.getPos event)
          player (.getPlayer event)
          hand (.getHand event)
          face (.getFace event)
          hit-result (.getHitVec event)]
      (when-let [result (events/on-block-activate dispatcher 
                                                 world 
                                                 pos 
                                                 player 
                                                 hand 
                                                 face 
                                                 hit-result)]
        (when (= (:type result) :open-gui)
          (.setCanceled event true))))))

(defn register-event-handlers [dispatcher]
  (let [handler (->ForgeEventHandler dispatcher)]
    (-> (MinecraftForge/EVENT_BUS)
        (.register handler))))