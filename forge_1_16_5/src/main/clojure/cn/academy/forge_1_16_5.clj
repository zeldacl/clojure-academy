(ns cn.academy.forge-1-16-5
  (:require [clojure.tools.logging :as log]
            [cn.academy.core :as core]
            [cn.academy.forge.v1_16_5.init :as init])
  (:import [net.minecraftforge.common MinecraftForge]
           [net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent]))

(defn setup-common []
  (log/info "Starting AcademyCraft for Forge 1.16.5")
  (log/info "https://ac.li-dev.cn/")
  (init/setup-common (FMLCommonSetupEvent.)))

(defn -init []
  (log/info "Initializing AcademyCraft Forge 1.16.5 integration")
  (init/init))