(ns cn.li.academy.core
  (:require
    ;[cn.li.mcmod.core :refer [defmod]]
    [cn.li.academy.proxy :as proxy]
    [clojure.tools.logging :as log])
  (:import (net.minecraftforge.fml.common Mod Mod$EventBusSubscriber Mod$EventBusSubscriber$Bus)
           (net.minecraftforge.eventbus.api SubscribeEvent EventPriority)
           (cn.test BlockRegistryEvent BlockRegistryEvent$Rrr)
           (net.minecraftforge.event RegistryEvent$Register)
           (org.apache.logging.log4j LogManager)
           (net.minecraftforge.fml.javafmlmod FMLJavaModLoadingContext)
           (net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent)
           (cn.li.mcmod BaseMod EventWrap$FMLCommonSetupEventWrap)))

;(defmod clj-academy
;        :modid "clj-academy"
;        :version "0.1.0"
;        :proxy {:client proxy/client-proxy
;                :server ""})

(def logger (LogManager/getLogger))

(gen-class
  :name ^{Mod "clojure-academy"} ClojureAcademy             ;(with-meta "ClojureAcademy" {Mod "clojure-academy"})
  :extends cn.li.mcmod.BaseMod
  :methods []
  :init "init"
  :prefix "mod-"
  :constructors {[] []})

(defn msetup [^{:final true} ^FMLCommonSetupEvent event]
  (log/info "###############################   "))


(def consumer (reify java.util.function.Consumer
                (accept [this t]
                  ; here the impl
                  (log/info "###############################   ")
                  )))

(def consumer1 (proxy [EventWrap$FMLCommonSetupEventWrap] []
                 (accept [^FMLCommonSetupEvent t]
                   ; here the impl
                   (log/info "###############################-------------   " t)
                   )))

;(gen-class
;  :name cn.li.academy.core.Qqq
;  :prefix "qq-"
;  :implements [BlockRegistryEvent$Rrr])
;
;(defn qq-accept [this t]
;  (log/info "###############################^^^^5235262525235   "))

(defn mod-init []
  (-> (FMLJavaModLoadingContext/get) .getModEventBus (.addListener consumer1))
  [[] (atom {})])


(gen-class
  :name ^{Mod$EventBusSubscriber {:bus Mod$EventBusSubscriber$Bus/MOD}} cn.li.academy.core.Caa
  :extends cn.test.BlockRegistryEvent
  :prefix "aa-"
  ;:methods [^{:static true} [^{SubscribeEvent {:priority EventPriority/NORMAL}} onBlocksRegistry [^{:final true} net.minecraftforge.event.RegistryEvent$Register] void]]
  )

(defn aa-onBlocksRegistry [event]
  (log/info "ddddddddddddddddddd1111   " (str (.getName event))))


(gen-class
  :name ^{Mod$EventBusSubscriber {:bus Mod$EventBusSubscriber$Bus/MOD}} cn.li.academy.core.Cbb
  :prefix "bb-"
  :methods [^{:static true} [^{SubscribeEvent {:priority EventPriority/NORMAL}} onBlocksRegistry [^{:final true} net.minecraftforge.event.RegistryEvent$Register] void]]
  )

(defn bb-onBlocksRegistry [event]
  (log/info "ddddddddddddddddddd22222222   " (str (.getName event)))
  (.info logger "rrrrrrrrrrrrrrrrrrrrrrr22222"))