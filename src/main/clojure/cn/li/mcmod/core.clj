(ns cn.li.mcmod.core
  (:require [cn.li.mcmod.network :refer [init-networks]]
            [cn.li.mcmod.common :refer [vec->map]]
            [cn.li.mcmod.utils :refer [get-fullname with-prefix]])
  ;(:import (net.minecraftforge.fml.common Mod Mod$EventHandler)
  ;         (net.minecraftforge.fml.common.event FMLPreInitializationEvent FMLInitializationEvent FMLPostInitializationEvent))
  (:import (cn.li.mcmod BaseMod EventWrap$FMLCommonSetupEventWrap)
           (net.minecraftforge.fml.javafmlmod FMLJavaModLoadingContext)
           (net.minecraftforge.fml.event.lifecycle FMLCommonSetupEvent)
           (net.minecraftforge.fml.common Mod)))


(defmacro create-obj-with-proxy [klass]
  `(proxy [~klass] [] (toString [] (str "proxyToString"))))
(create-obj-with-proxy java.lang.Object)

(defmacro defclass
  ([class-name super-class class-data]
   (let [name-ns (get class-data :ns *ns*)
         prefix (str class-name "-")
         fullname (get class-data :fullname (get-fullname name-ns class-name))
         class-data (dissoc class-data :fullname)
         class-data (reduce concat [] (into [] class-data))]
     `(do
        (gen-class
          :name   ~fullname                                        ;~(with-meta fullname `{Mod "ddd"})
          :prefix ~prefix
          :extends ~super-class
          ~@class-data)))))


(defmacro generate-event-fn [wrap event-name fn]
  `(clojure.core/proxy [~wrap] []
     (~'accept [~'t]                                           ;(with-meta ~'t {~event-name :true})
       ; here the impl
       ~(if fn
          `(~fn ~'t)
          nil))))

(defmacro defmod [mod-name & options]
  (let [full-name mod-name
        options-map (vec->map options)
        ;mod-meta {:name ""
        ;          :modid (str mod-name)
        ;          :version (str version)
        ;          :modLanguage "clojure"
        ;          }
        s (get-in options-map [:events :setup])
        options-map (assoc options-map :init 'initialize
                                       :constructors {[] []})
        addListener (fn [consumer] `(-> (FMLJavaModLoadingContext/get) .getModEventBus (.addListener ~consumer)))
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
        ;setup-fn `(proxy [EventWrap$FMLCommonSetupEventWrap] []
        ;        (~'accept [~'^FMLCommonSetupEvent t]
        ;          ; here the impl
        ;          ~(when-let [s (get-in options-map [:events :setup])]
        ;             `(~s ~'t)
        ;             )
        ;          ))
        prefix (str mod-name "-")
        options-map (dissoc options-map :events)
        name-ns (get options-map :ns *ns*)
        fullname (get-fullname name-ns mod-name)
        options-map (assoc options-map :fullname (with-meta fullname {Mod (get options-map :modid)}))]
    `(do
       (defclass
         ~mod-name
         BaseMod
         ~options-map)
       (with-prefix ~prefix
         (defn ~'initialize
           ([~'& ~'args]
            ~(addListener setup-fn)
            [(into [] ~'args) (atom {})]))))))