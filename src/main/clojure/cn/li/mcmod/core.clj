(ns cn.li.mcmod.core
  (:require [cn.li.mcmod.network :refer [init-networks]]
    ;[cn.li.mcmod.common :refer [vec->map]]
            [cn.li.mcmod.utils :refer [get-fullname with-prefix vec->map]]
            [clojure.tools.logging :as log]
            [cn.li.mcmod.registry :refer [on-blocks-registry on-items-registry on-blocks-container-registry on-tile-entity-registry]])
  ;(:import (net.minecraftforge.fml.common Mod Mod$EventHandler)
  ;         (net.minecraftforge.fml.common.event FMLPreInitializationEvent FMLInitializationEvent FMLPostInitializationEvent))
  (:import                                                  ;(EventWrap$FMLCommonSetupEventWrap EventWrap$InterModEnqueueEventWrap EventWrap$InterModProcessEventWrap EventWrap$FMLClientSetupEventWrap)
    (net.minecraftforge.fml.javafmlmod FMLJavaModLoadingContext)
    (net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent InterModEnqueueEvent InterModProcessEvent FMLClientSetupEvent)
    (net.minecraftforge.fml.common Mod Mod$EventBusSubscriber$Bus Mod$EventBusSubscriber)
    (net.minecraftforge.eventbus.api SubscribeEvent EventPriority)
    (net.minecraftforge.fml.event.server FMLServerStartingEvent)
    (net.minecraftforge.common MinecraftForge)
    (cn.li.mcmod EventWrap$FMLClientSetupEventWrap EventWrap$FMLCommonSetupEventWrap EventWrap$InterModEnqueueEventWrap EventWrap$InterModProcessEventWrap)))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)
;(set! *unchecked-math* :warn-on-boxed)

;(defmacro create-obj-with-proxy [klass]
;  `(proxy [~klass] [] (toString [] (str "proxyToString"))))
;(create-obj-with-proxy java.lang.Object)




(defmacro generate-event-fn [wrap event-name fn]
  `(clojure.core/proxy [~wrap] []
     (~'accept [~'t]                                           ;(with-meta ~'t {~event-name :true})
       ; here the impl
       (log/info "12345666666666666666666666666666666666666666666")
       ~(if fn
          `(~fn ~'t)
          nil))))


(defn addListener [consumer]
  (-> (FMLJavaModLoadingContext/get) .getModEventBus (.addListener consumer)))

;(defn generate-event-fn [wrap event-name fn]
;  (clojure.core/proxy [wrap] []
;     (accept [t]                                           ;(with-meta ~'t {~event-name :true})
;       ; here the impl
;       (if fn
;          (fn t)
;          nil))))

(defmacro defmod [mod-name & options]
  (let [                                                    ;full-name mod-name
        options-map (vec->map options)
        ;mod-meta {:name ""
        ;          :modid (str mod-name)
        ;          :version (str version)
        ;          :modLanguage "clojure"
        ;          }
        ;s (get-in options-map [:events :setup])
        options-map (assoc options-map :init 'initialize
                                       :constructors {[] []})
        ;addListener (fn [consumer] `(-> (FMLJavaModLoadingContext/get) .getModEventBus (.addListener ~consumer)))
        ;generate-event-fn (fn [wrap event-name key]
        ;                    ~(clojure.core/proxy [~wrap] []
        ;                       (accept [t]
        ;                         ; here the impl
        ;                         (when-let [s (get-in options-map [:events ~key])]
        ;                            ;(~s ~'t)
        ;                            )
        ;                         )))
        ;generate-event-fn (fn [wrap event-name key]
        ;                    `(clojure.core/proxy [~wrap] []
        ;                       (~'accept [~'t]
        ;                         ; here the impl
        ;                         ~(when-let [s (get-in options-map [:events key])]
        ;                            `(~s ~'t)
        ;                            )
        ;                         )))
        ;(with-meta ~'t {~event-name :true})
        setup-fn `(generate-event-fn EventWrap$FMLCommonSetupEventWrap FMLCommonSetupEvent ~(get-in options-map [:events :setup]))
        mod-enqueue-fn `(generate-event-fn EventWrap$InterModEnqueueEventWrap InterModEnqueueEvent ~(get-in options-map [:events :mod-enqueue]))
        mod-process-fn `(generate-event-fn EventWrap$InterModProcessEventWrap InterModProcessEvent ~(get-in options-map [:events :mod-process]))
        do-client-stuff-fn `(generate-event-fn EventWrap$FMLClientSetupEventWrap FMLClientSetupEvent ~(get-in options-map [:events :do-client-stuff]))
        ;setup-fn `(proxy [EventWrap$FMLCommonSetupEventWrap] []
        ;        (~'accept [~'^FMLCommonSetupEvent t]
        ;          ; here the impl
        ;          ~(when-let [s (get-in options-map [:events :setup])]
        ;             `(~s ~'t)
        ;             )
        ;          ))
        prefix (str mod-name "-")
        ;options-map (dissoc options-map :events)
        name-ns (get options-map :ns *ns*)
        fullname (get-fullname name-ns mod-name)]
        ;options-map (assoc options-map :fullname `(with-meta ~fullname {Mod ~(get options-map :modid)}))]
    `(do
       ;; don't known how to do
       ;(defclass
       ;  ~mod-name
       ;  BaseMod
       ;  ~options-map)
       ;~(get-in options-map [:events :server-starting])
       (gen-class
         :name ~(with-meta fullname `{Mod ~(str mod-name)})
         :prefix ~(symbol prefix)
         ;:extends BaseMod
         :init ~'initialize
         :post-init ~'post-initialize
         :constructors {[] []}
         :methods [[~(with-meta 'onServerStarting `{SubscribeEvent []}) [FMLServerStartingEvent] ~'void]])
       (with-prefix ~prefix
         (defn ~'initialize
           ([~'& ~'args]
            ;(addListener (generate-event-fn ~'EventWrap$FMLCommonSetupEventWrap ~'FMLCommonSetupEvent ~(get-in options-map [:events :setup])))
            (addListener ~setup-fn)
            (addListener ~mod-enqueue-fn)
            (addListener ~mod-process-fn)
            (addListener ~do-client-stuff-fn)
            [(into [] ~'args) (atom {})]))
         (defn ~'post-initialize [~'obj ~'& ~'args]
           (.register MinecraftForge/EVENT_BUS ~'obj))
         (defn ~'onServerStarting [~'this ~'event]
           ~(when (get-in options-map [:events :server-starting])
              `(~(get-in options-map [:events :server-starting]) ~'this ~'event))))
       ;(defn ~(symbol (str prefix "initialize"))
       ;  ([~'& ~'args]
       ;   ;(addListener (generate-event-fn ~'EventWrap$FMLCommonSetupEventWrap ~'FMLCommonSetupEvent ~(get-in options-map [:events :setup])))
       ;   ('addListener ~setup-fn)
       ;   ('addListener ~mod-enqueue-fn)
       ;   ('addListener ~mod-process-fn)
       ;   ('addListener ~do-client-stuff-fn)
       ;   [(into [] ~'args) (atom {})]))
       )))


;(gen-class
;  :name ^{Mod$EventBusSubscriber {:bus Mod$EventBusSubscriber$Bus/MOD}} cn.li.academy.core.Cbb
;  :prefix "bb-"
;  :methods [^{:static true} [^{SubscribeEvent {:priority EventPriority/NORMAL}} onBlocksRegistry [^{:final true} net.minecraftforge.event.RegistryEvent$Register] void]]
;  )
;
;(defn bb-onBlocksRegistry [^net.minecraftforge.event.RegistryEvent$Register event]
;  (log/info "ddddddddddddddddddd22222222   " (str (.getName event)))
;  ;(.info logger "rrrrrrrrrrrrrrrrrrrrrrr22222")
;  )

(let [class-name "mcmod-registry"
      prefix (str class-name "-")
      full-name (get-fullname *ns* class-name)]
  (gen-class
    :name ^{Mod$EventBusSubscriber {:bus Mod$EventBusSubscriber$Bus/MOD}} cn.li.mcmod.core.McmodRegistry
    :prefix prefix
    :methods [^{:static true} [^{SubscribeEvent {:priority EventPriority/NORMAL}} onBlocksRegistry [^{:final true} net.minecraftforge.event.RegistryEvent$Register] void]]
    )
  (with-prefix prefix
    (defn onBlocksRegistry [^net.minecraftforge.event.RegistryEvent$Register event]
      (log/info "ddddddddddddddddddd123123   " (str (.getName event)))
      (condp = (str (.getName event))
        "minecraft:block" (on-blocks-registry event)
        "minecraft:item" (on-items-registry event)
        "minecraft:entity_type" nil
        "minecraft:fluid" nil
        "minecraft:block_entity_type" (on-tile-entity-registry event)                   ;tile-entity
        "minecraft:menu" (on-blocks-container-registry event)                                ;container
        "minecraft:recipe_serializer" nil                   ;ShapedRecipe
        "minecraft:mob_effect" nil                          ;
        "minecraft:biome" nil
        "minecraft:sound_event" nil
        "minecraft:enchantment" nil
        (log/debug "wwwwwwwwwwwwwwwww123123   " (str (.getName event))))
      ;(.info logger "rrrrrrrrrrrrrrrrrrrrrrr22222")
      )))


