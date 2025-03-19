(ns forge-impl.test.integration-utils
  (:require [mcmod.test-utils :as test])
  (:import [net.minecraft.world.server ServerWorld]
           [net.minecraft.util.math BlockPos]
           [net.minecraftforge.energy CapabilityEnergy]
           [net.minecraftforge.fml.server ServerLifecycleHooks]))

(defn get-server-world []
  (-> (ServerLifecycleHooks/getCurrentServer)
      (.getWorld net.minecraft.world.World$OVERWORLD)))

(defn create-integration-pos [^BlockPos pos]
  {:x (.getX pos)
   :y (.getY pos)
   :z (.getZ pos)})

(defn get-energy-capability [tile-entity]
  (.getCapability tile-entity CapabilityEnergy/ENERGY nil))

(defmacro with-integration-world [[world-sym] & body]
  `(let [~world-sym (get-server-world)]
     ~@body))