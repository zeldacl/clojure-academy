(ns cn.li.academy.core
  (:require [forge-clj.core :refer [defmod]])
  (:import (cpw.mods.fml.common Mod Mod$EventHandler Mod$Instance)
           (cpw.mods.fml.common.event FMLInitializationEvent FMLPreInitializationEvent FMLPostInitializationEvent FMLServerStartingEvent)
           (org.apache.logging.log4j LogManager))
  (:use [cn.li.utils.log :only [log-info log-warn]]))

;Creates the mod itself, passing in the common-init function as the initializing function for the mod's common proxy.
(defmod clojureacademy "0.0.1"
        :common {:init cn.li.academy.common/common-init
                 :pre-init cn.li.academy.common/common-pre-init
                 :post-init cn.li.academy.common/common-post-init
                 }
        :client {:init cn.li.academy.client/client-init}
        ;:repl true
        )

