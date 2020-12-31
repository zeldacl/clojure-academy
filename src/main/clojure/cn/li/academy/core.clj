(ns cn.li.academy.core
  (:require
    [cn.li.mcmod.core :refer [defmod]]
    [cn.li.academy.proxy :as proxy]
    [cn.li.mcmod.log :as mcmodlog]
    [clojure.tools.logging :as log]
    [cn.li.academy.ac-registry :as ac-blocks]
    [cn.li.academy.global]
    [cn.li.mcmod.item :as item]
    [cn.li.mcmod.registry :as registry]
    [cn.li.mcmod.network :as network]
    [cn.li.academy.energy.blocks.node :as node]
    ;[cn.li.academy.client.ac-registry]
    [cn.li.academy.ac-registry :refer [init-registry]]
    ;[cn.li.mcmod.client.registry :refer [on-screens-registry]]
    )
  (:import (net.minecraftforge.fml.common Mod Mod$EventBusSubscriber Mod$EventBusSubscriber$Bus)
           (net.minecraftforge.eventbus.api SubscribeEvent EventPriority)
    ;(cn.test BlockRegistryEvent BlockRegistryEvent$Rrr)
           (net.minecraftforge.event RegistryEvent$Register)
           (org.apache.logging.log4j LogManager)
           (net.minecraftforge.fml.javafmlmod FMLJavaModLoadingContext)
           (net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent InterModEnqueueEvent InterModProcessEvent FMLClientSetupEvent)
    ;(EventWrap$FMLCommonSetupEventWrap EventWrap$InterModEnqueueEventWrap)
           (net.minecraft.block Blocks)
           (net.minecraftforge.fml DistExecutor)
           (java.util.function Supplier)
           (net.minecraft.util ResourceLocation)))

;(defmod aaa)

;(defn bbb [wrap event-name key]
;  (clojure.core/proxy [~wrap] []
;    (accept [t]
;      ; here the impl
;      (when-let [s (get-in options-map [:events key])]
;        ;(~s ~'t)
;        )
;      )))

;(cn.li.mcmod.utils/with-prefix
;  "aaa-"
;  (defn
;    initialize
;    ([& args]
;     (->
;       (FMLJavaModLoadingContext/get)
;       .getModEventBus
;       (.addListener
;         (proxy [EventWrap$FMLCommonSetupEventWrap] []
;           (accept [(with-meta t {FMLCommonSetupEvent :true})] nil))))
;     [(into [] args) (atom {})])))
;(cn.li.mcmod.core/defclass aaa BaseMod {:init initialize})

;(cn.li.mcmod.core/generate-event-fn
;  EventWrap$InterModEnqueueEventWrap
;  InterModEnqueueEvent
;  (fn [^InterModEnqueueEvent e] (log/info "HELLO FROM InterModEnqueueEvent cljacademy")))

;(def consumer2 (proxy [EventWrap$InterModEnqueueEventWrap] []
;                 (accept [^InterModEnqueueEvent t]
;                   ; here the impl
;                   (log/info "###############################-------------   " t)
;                   )))
(def logger (LogManager/getLogger))

;(mcmodlog/init-log)
(item/init-item-group "cljacademy" "logo")
;(init-registry)

(defn setup-fn [^FMLCommonSetupEvent e]
  (.println System/out "777777777777777777777777777777777777777777")
  (.println System/err "888888888888888888888888888888888888888888")

  (network/init-networks (ResourceLocation. "cljacademy" "cljacademy"))
  ;(DistExecutor/runForDist
  ;  (proxy [Supplier] []
  ;    (get []
  ;      (proxy [Supplier] []
  ;        (get []
  ;          (log/info "HELLO FROM PREINIT cljacademy client 1111111111111111111")
  ;          ;(cn.li.academy.client.ac-registry/init)
  ;          ;(on-screens-registry)
  ;          ))))
  ;  (proxy [Supplier] []
  ;    (get []
  ;      (proxy [Supplier] []
  ;        (get []
  ;          (log/info "HELLO FROM PREINIT cljacademy server 22222222222222222222222"))))))
  (log/info "HELLO FROM PREINIT cljacademy" (.name log/*logger-factory*))
  (log/info "HELLO FROM PREINIT cljacademy")
  (log/info "DIRT BLOCK >> {}" (.getRegistryName Blocks/DIRT))
  (.info logger "rrrrrrrrrrrrrrrrrrrrrrr22222")
  ;(throw "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqq")
  )

(defn mod-enqueue-fn [^InterModEnqueueEvent e]
  (.info logger "rrrrrrrrrrrrrrrrrrrrrrr5555555555555555555")
  (log/info "HELLO FROM InterModEnqueueEvent cljacademy"))

(defn mod-process-fn [^InterModProcessEvent e]
  (log/info "HELLO FROM InterModProcessEvent cljacademy"))

(defn do-client-stuff-fn [^FMLClientSetupEvent e]
  (log/info "HELLO FROM FMLClientSetupEvent  cljacademy"))

(defn server-starting-fn [this e]
  (log/info "HELLO FROM FMLServerStartingEvent  cljacademy"))

(defmod cljacademy
        ;:modid "cljacademy"
        ;:version "0.1.0"
        ;:events {:setup           (fn [^FMLCommonSetupEvent e]
        ;                      (log/info "HELLO FROM PREINIT cljacademy")
        ;                      (log/info "DIRT BLOCK >> {}" (.getRegistryName Blocks/DIRT)))
        ;   :mod-enqueue     (fn [^InterModEnqueueEvent e]
        ;                      (log/info "HELLO FROM InterModEnqueueEvent cljacademy"))
        ;   :mod-process     (fn [^InterModProcessEvent e]
        ;                      (log/info "HELLO FROM InterModProcessEvent cljacademy"))
        ;   :do-client-stuff (fn [^FMLClientSetupEvent e]
        ;                      (log/info "HELLO FROM FMLClientSetupEvent  cljacademy"))
        ;   }
        :events {:setup           setup-fn
                 :mod-enqueue     mod-enqueue-fn
                 :mod-process     mod-process-fn
                 :do-client-stuff do-client-stuff-fn
                 :server-starting server-starting-fn
                 })

;(cn.li.mcmod.utils/with-prefix
;  "cljacademy-"
;  (clojure.core/defn
;    initialize
;    ([& args]
;     (cn.li.mcmod.core/addListener
;       (cn.li.mcmod.core/generate-event-fn EventWrap$FMLCommonSetupEventWrap FMLCommonSetupEvent setup-fn))
;     (cn.li.mcmod.core/addListener
;       (cn.li.mcmod.core/generate-event-fn EventWrap$InterModEnqueueEventWrap InterModEnqueueEvent mod-enqueue-fn))
;     (cn.li.mcmod.core/addListener
;       (cn.li.mcmod.core/generate-event-fn
;         cn.li.mcmod.EventWrap$InterModProcessEventWrap
;         InterModProcessEvent
;         mod-process-fn))
;     (cn.li.mcmod.core/addListener
;       (cn.li.mcmod.core/generate-event-fn
;         cn.li.mcmod.EventWrap$FMLClientSetupEventWrap
;         FMLClientSetupEvent
;         do-client-stuff-fn))
;     [(clojure.core/into [] args) (clojure.core/atom {})])))
;(cn.li.mcmod.core/generate-event-fn EventWrap$FMLCommonSetupEventWrap FMLCommonSetupEvent setup-fn)
;
;(let* [p# (new cn.li.academy.core.proxy$cn.li.mcmod.EventWrap$FMLCommonSetupEventWrap$ff19274a)]
;  (clojure.core/init-proxy p# {"accept" (clojure.core/fn ([this t] (setup-fn t)))})
;  p#)
;(let* [p# (new cn.li.academy.core.proxy$cn.li.mcmod.EventWrap$InterModEnqueueEventWrap$ff19274a)]
;  (clojure.core/init-proxy p# {"accept" (clojure.core/fn ([this t] (mod-enqueue-fn t)))})
;  p#)
;
;(cn.li.mcmod.core/generate-event-fn EventWrap$InterModEnqueueEventWrap InterModEnqueueEvent mod-enqueue-fn)

;(def logger (LogManager/getLogger))

;(gen-class
;  :name ^{Mod "cljacademy"} ClojureAcademy             ;(with-meta "ClojureAcademy" {Mod "cljacademy"})
;  :extends cn.li.mcmod.BaseMod
;  :methods []
;  :init "init"
;  :prefix "mod-"
;  :constructors {[] []})
;
;(defn msetup [^{:final true} ^FMLCommonSetupEvent event]
;  (log/info "###############################   "))
;
;
;(def consumer (reify java.util.function.Consumer
;                (accept [this t]
;                  ; here the impl
;                  (log/info "###############################   ")
;                  )))
;
;(def consumer1 (proxy [EventWrap$FMLCommonSetupEventWrap] []
;                 (accept [^FMLCommonSetupEvent t]
;                   ; here the impl
;                   (log/info "###############################-------------   " t)
;                   )))
;
;;(gen-class
;;  :name cn.li.academy.core.Qqq
;;  :prefix "qq-"
;;  :implements [BlockRegistryEvent$Rrr])
;;
;;(defn qq-accept [this t]
;;  (log/info "###############################^^^^5235262525235   "))
;
;(defn mod-init []
;  (-> (FMLJavaModLoadingContext/get) .getModEventBus (.addListener consumer1))
;  [[] (atom {})])
;
;
;(gen-class
;  :name ^{Mod$EventBusSubscriber {:bus Mod$EventBusSubscriber$Bus/MOD}} cn.li.academy.core.Caa
;  :extends cn.test.BlockRegistryEvent
;  :prefix "aa-"
;  ;:methods [^{:static true} [^{SubscribeEvent {:priority EventPriority/NORMAL}} onBlocksRegistry [^{:final true} net.minecraftforge.event.RegistryEvent$Register] void]]
;  )
;
;(defn aa-onBlocksRegistry [event]
;  (log/info "ddddddddddddddddddd1111   " (str (.getName event))))
;
;
;(gen-class
;  :name ^{Mod$EventBusSubscriber {:bus Mod$EventBusSubscriber$Bus/MOD}} cn.li.academy.core.Cbb
;  :prefix "bb-"
;  :methods [^{:static true} [^{SubscribeEvent {:priority EventPriority/NORMAL}} onBlocksRegistry [^{:final true} net.minecraftforge.event.RegistryEvent$Register] void]]
;  )
;
;(defn bb-onBlocksRegistry [event]
;  (log/info "ddddddddddddddddddd22222222   " (str (.getName event)))
;  (.info logger "rrrrrrrrrrrrrrrrrrrrrrr22222"))