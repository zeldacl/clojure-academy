(ns cn.academy.core
  (:require [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [net.minecraftforge.common :as forge]
            [net.minecraftforge.fml.common :as fml]
            [net.minecraftforge.fml.common.event :as event]
            [net.minecraftforge.fml.common.network :as network]
            [net.minecraftforge.oredict :as oredict])
  (:import [cn.academy ACItems]
           [cn.lambdalib2.crafting RecipeRegistry]
           [cn.lambdalib2.registry RegistryMod StateEventCallback]
           [net.minecraft.creativetab CreativeTabs]
           [net.minecraft.item ItemStack]
           [net.minecraft.util ResourceLocation]
           [org.apache.logging.log4j LogManager]))

(def ^:const VERSION "@VERSION@")
(def ^:const DEBUG_MODE (.startsWith VERSION "@"))
(def log (LogManager/getLogger "AcademyCraft"))
(def scripts ["generic" "ability" "electromaster" "teleporter" "meltdowner" "generic_skills"])
(def config (atom nil))
(def recipes (atom nil))
(def net-handler (network/NetworkRegistry/INSTANCE.newSimpleChannel "academy-network"))
(def analytic-data-listener (atom nil))

(def cct
  (proxy [CreativeTabs] ["AcademyCraft"]
    (createIcon []
      (ItemStack. ACItems/logo))))

(defn pre-init [^event/FMLPreInitializationEvent e]
  (log/info "Starting AcademyCraft")
  (log/info "Copyright (c) Lambda Innovation, 2013-2018")
  (log/info "https://ac.li-dev.cn/")
  (reset! recipes (RecipeRegistry.))
  (reset! config (Configuration. (.getSuggestedConfigurationFile e)))
  (when (.getBoolean @config "analysis" "generic" true "switch for analytic system")
    (reset! analytic-data-listener AnalyticDataListener/instance)))

(defn init [^event/FMLInitializationEvent e]
  (forge/MinecraftForge/EVENT_BUS.register this)
  (oredict/OreDictionary/registerOre "plateIron" ACItems/reinforced_iron_plate))

(defn post-init [^event/FMLPostInitializationEvent e]
  (.addRecipeFromResourceLocation @recipes (ResourceLocation. "academy:recipes/default.recipe"))
  (when DEBUG_MODE
    (log/info "|-------------------------------------------------------")
    (log/info "| AC Recipe Name Mappings")
    (log/info "|--------------------------|----------------------------")
    (log/info (format "| %-25s| Object Name" "Recipe Name"))
    (log/info "|--------------------------|----------------------------")
    (doseq [[k v] (.getNameMappingForDebug @recipes)]
      (let [name (cond
                   (instance? Item v) (.translateToLocal (str (.getTranslationKey ^Item v) ".name"))
                   (instance? Block v) (.translateToLocal (str (.getTranslationKey ^Block v) ".name"))
                   :else (str v))]
        (log/info (format "| %-25s| %s" k name))))
    (log/info "|-------------------------------------------------------"))
  (reset! recipes nil)
  (.save @config))

(defn server-starting [^event/FMLServerStartingEvent e]
  (ACConfig/updateConfig nil))

(defn server-stopping [^event/FMLServerStoppingEvent e]
  (.save @config))

(defn on-client-disconnection-from-server [^network/FMLNetworkEvent$ClientDisconnectionFromServerEvent e]
  (.save @config))

(defn add-to-recipe [klass]
  (CustomMappingHelper/addMapping @recipes klass))

(defn debug [msg]
  (when DEBUG_MODE
    (log/info msg)))

(fml/Mod. "academy" "AcademyCraft" VERSION
  :dependencies "required-after:lambdalib2@@LAMBDA_LIB_VERSION@"
  :rootPackage "cn.academy."
  :resourceDomain "academy")

(RegistryMod. "cn.academy." "academy")