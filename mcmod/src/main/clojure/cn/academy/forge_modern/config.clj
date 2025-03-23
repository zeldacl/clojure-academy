(ns cn.academy.forge-modern.config
  (:require [cn.academy.core.config :as core-config])
  (:import [net.minecraftforge.fml.config ModConfig$Type ConfigBuilder]
           [net.minecraftforge.common ForgeConfigSpec ForgeConfigSpec$Builder]
           [java.util.function Consumer]))

(defn- create-builder []
  (let [builder (ForgeConfigSpec$Builder.)]
    (.push builder "cat_engine")
    (let [energy-gen-rate (.define builder "energy_gen_rate" 
                                  (Double. 5.0)
                                  #(instance? Double %))
          max-energy (.define builder "max_energy"
                             (Double. 100000.0)
                             #(instance? Double %))
          wireless-range (.defineInRange builder "wireless_range"
                                       (Integer. 16)
                                       (Integer. 1)
                                       (Integer. 64))
          allow-interdimensional (.define builder "allow_interdimensional"
                                        true
                                        #(instance? Boolean %))]
      (.pop builder)
      {:spec (.build builder)
       :values {:energy-gen-rate energy-gen-rate
                :max-energy max-energy
                :wireless-range wireless-range
                :allow-interdimensional allow-interdimensional}})))

(defn create-mod-config [mod-id]
  (let [{:keys [spec values]} (create-builder)]
    (.accept (reify Consumer
               (accept [_ _]
                 (core-config/load-config! 
                   {:cat-engine
                    {:energy-gen-rate (.get (:energy-gen-rate values))
                     :max-energy (.get (:max-energy values))
                     :wireless-range (.get (:wireless-range values))
                     :allow-interdimensional (.get (:allow-interdimensional values))}})))
             nil)
    {:spec spec
     :type ModConfig$Type/COMMON
     :file-name (str mod-id ".toml")}))