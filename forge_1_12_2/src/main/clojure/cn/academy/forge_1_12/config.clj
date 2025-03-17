(ns cn.academy.forge-1-12.config
  (:require [cn.academy.core.config :as core-config])
  (:import [net.minecraftforge.common.config Configuration]
           [java.io File]))

(defn- create-category! [^Configuration config category-name comment]
  (.get config Configuration/CATEGORY_GENERAL category-name))

(defn- get-property [^Configuration config category name default type comment]
  (case type
    :double (.get config category name default (into-array String [comment]))
    :int (.get config category name (int default) (into-array String [comment]))
    :boolean (.get config category name default (into-array String [comment]))
    :string (.get config category name default (into-array String [comment]))))

(defn load-config! [^File config-file]
  (let [config (Configuration. config-file)
        cat-engine-category (create-category! config "cat_engine" "Cat Engine Settings")]
    
    (try
      (.load config)
      (let [config-data 
            {:cat-engine
             {:energy-gen-rate (.getDouble (get-property config cat-engine-category 
                                                       "energyGenRate" 5.0 :double 
                                                       "Energy generated per tick"))
              :max-energy (.getDouble (get-property config cat-engine-category 
                                                  "maxEnergy" 100000.0 :double 
                                                  "Maximum energy storage"))
              :wireless-range (.getInt (get-property config cat-engine-category 
                                                   "wirelessRange" 16 :int 
                                                   "Range for wireless transmission"))
              :allow-interdimensional (.getBoolean (get-property config cat-engine-category 
                                                               "allowInterdimensional" true :boolean 
                                                               "Allow interdimensional energy transfer"))}}]
        (core-config/load-config! config-data))
      (finally
        (when (.hasChanged config)
          (.save config))))
    config))