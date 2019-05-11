(ns cn.li.academy.core
  (:require
    ;[cn.li.mcmod.core :refer [defmod]]
    [cn.li.academy.proxy :as proxy]
    [clojure.tools.logging :as log])
  (:import (net.minecraftforge.fml.common Mod Mod$EventBusSubscriber Mod$EventBusSubscriber$Bus)
           (net.minecraftforge.eventbus.api SubscribeEvent)
           (cn.test BlockRegistryEvent)))

;(defmod clj-academy
;        :modid "clj-academy"
;        :version "0.1.0"
;        :proxy {:client proxy/client-proxy
;                :server ""})


(gen-class
  :name "ClojureAcademy"                                                    ;(with-meta "ClojureAcademy" {Mod "clojure-academy"})
  :methods [])


(gen-class
  :name ^{Mod$EventBusSubscriber {:bus Mod$EventBusSubscriber$Bus/MOD}} RegistryEvents
  :prefix "aa-"
  :methods [^{SubscribeEvent []} [onBlocksRegistry [cn.test.BlockRegistryEvent] void]]
  )

(defn aa-onBlocksRegistry [this event]
  (log/info "ddddddddddddddddddd"))