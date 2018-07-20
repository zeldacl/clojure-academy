(ns cn.li.mcmod.core
  (:require [cn.li.mcmod.network :refer [init-networks]])
  (:import (net.minecraftforge.fml.common Mod Mod$EventHandler)
           (net.minecraftforge.fml.common.event FMLPreInitializationEvent FMLInitializationEvent FMLPostInitializationEvent)))


(defmacro defmod [mod-name version & options]
  (let [full-name mod-name
        prefix (str mod-name "-")]
    `(do
       (gen-class
         :name ~(with-meta full-name `{Mod {:modid ~(str mod-name) :version ~(str version)}})
         :prefix ~(symbol prefix)
         :methods [[~(with-meta 'preInit `{Mod$EventHandler []}) [FMLPreInitializationEvent] ~'void]
                   [~(with-meta 'init `{Mod$EventHandler []}) [FMLInitializationEvent] ~'void]
                   [~(with-meta 'preInit `{Mod$EventHandler []}) [FMLPostInitializationEvent] ~'void]])
       (defn ~(symbol (str prefix "preInit")) [~'this ~'event]
         (init-networks))
       (defn ~(symbol (str prefix "init")) [~'this ~'event])
       (defn ~(symbol (str prefix "postInit")) [~'this ~'event]))))