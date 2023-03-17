(ns cn.li.mcmod.client.registry
  (:require [clojure.tools.logging :as log]
            [cn.li.mcmod.utils :refer [construct]])
  (:import (net.minecraft.client.gui ScreenManager ScreenManager$IScreenFactory)
           (net.minecraft.inventory.container ContainerType)
           (net.minecraft.util.text ITextComponent)
           (net.minecraft.entity.player PlayerInventory)))



(defonce ^:dynamic *registry-screens* (atom []))
(defn registry-screen [block-container-type screen]
  (swap! *registry-screens* conj {                          ;:container block-container
                                  :container-type block-container-type :screen screen}))

(defn on-screens-registry []
  (log/info "qqqqqqqqqqqqqqqqqqq5556666  " *registry-screens*)
  (dorun (map (fn [{                                        ;container :container
                    container-type :container-type screen :screen}]
                (ScreenManager/register ^ContainerType container-type
                  (reify ScreenManager$IScreenFactory
                    (create [this container inv name]
                      (construct screen container inv name)))))
           (deref *registry-screens*)))

  )
