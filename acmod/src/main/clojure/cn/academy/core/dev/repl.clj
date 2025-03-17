(ns cn.academy.core.dev.repl
  (:require [cn.academy.core.util.logging :refer [log-info log-debug log-error]]
            [cn.academy.core.util.dev :as dev]
            [cn.academy.core.util.monitoring :as monitoring]
            [cn.academy.core.config :as config]
            [clojure.pprint :refer [pprint]])
  (:import [java.io File]
           [net.minecraft.util.math BlockPos]))

(defn reload-dev! []
  (dev/with-dev-mode
    (dev/reload-all!)
    (monitoring/reset-metrics!)
    :reloaded))

(defn watch-dev-config! [config-path]
  (dev/watch-config! (File. config-path)))

(defn show-metrics []
  (pprint (monitoring/get-metrics)))

(defn get-block-info [world pos]
  (let [block-state (.getBlockState world pos)
        block (.getBlock block-state)
        tile (.getTileEntity world pos)]
    {:block (.getRegistryName block)
     :meta (.getMetaFromState block block-state)
     :tile-entity (when tile
                   {:class (.getClass tile)
                    :nbt (let [nbt (net.minecraft.nbt.NBTTagCompound.)]
                          (.writeToNBT tile nbt)
                          nbt)})}))

(defn set-cat-engine-config! [options]
  (config/set-config! [:cat-engine] 
    (merge (config/get-config [:cat-engine] {})
           options))
  :updated)

(defn inspect-energy-network [world pos range]
  (let [center (if (instance? BlockPos pos)
                 pos
                 (BlockPos. (:x pos) (:y pos) (:z pos)))
        nodes (for [x (range (- range) (inc range))
                   y (range (- range) (inc range))
                   z (range (- range) (inc range))
                   :let [pos (BlockPos/add center x y z)
                         tile (.getTileEntity world pos)]
                   :when (and tile 
                             (.hasCapability tile
                               net.minecraftforge.energy.CapabilityEnergy/ENERGY
                               nil))]
               {:pos pos
                :energy (.getEnergyStored 
                         (.getCapability tile 
                           net.minecraftforge.energy.CapabilityEnergy/ENERGY
                           nil))})]
    (doseq [node nodes]
      (println (format "Energy at %s: %d FE"
                      (str (:pos node))
                      (:energy node))))
    {:total-nodes (count nodes)
     :total-energy (reduce + (map :energy nodes))}))