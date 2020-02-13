(ns cn.li.mcmod.client.setup
  (:require [cn.li.mcmod.utils :refer [with-prefix]]
            [cn.li.academy.client.ac-registry :refer [init]]
            [cn.li.mcmod.client.registry :refer [on-screens-registry]]
            [clojure.tools.logging :as log])
  (:import (net.minecraftforge.fml.common Mod$EventBusSubscriber Mod$EventBusSubscriber$Bus)
           (net.minecraftforge.api.distmarker Dist)
           (net.minecraftforge.eventbus.api EventPriority)))

(gen-class
  :name ^{Mod$EventBusSubscriber {:modid "cljacademy" :value [Dist/CLIENT]  :bus Mod$EventBusSubscriber$Bus/MOD}} cn.li.mcmod.client.ClientSetup
  :prefix "client-setup-"
  :methods [^{:static true} [clientSetup [^{:final true} net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent] void]]
  )
(with-prefix "client-setup-"
  (defn clientSetup [^net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event]
    ;(log/info "ddddddddddddddddddd123123   " (str (.getName event)))
    (init)
    (on-screens-registry)
    ;(.info logger "rrrrrrrrrrrrrrrrrrrrrrr22222")
    ))
