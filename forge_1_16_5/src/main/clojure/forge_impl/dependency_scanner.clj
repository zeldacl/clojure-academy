(ns forge-impl.dependency-scanner
  (:require [mcmod.protocols :refer :all])
  (:import [net.minecraftforge.fml ModList]
           [net.minecraftforge.forgespi.language IModInfo]))

(defn get-loaded-mods []
  (let [mod-list (ModList/get)]
    (->> (.getMods mod-list)
         (map #(.getModId ^IModInfo %)))))

(defn validate-dependencies! []
  (let [loaded-mods (get-loaded-mods)]
    (when-not (some #{"mcmod"} loaded-mods)
      (throw (RuntimeException. "Required mod 'mcmod' is not loaded!")))
    (when-not (some #{"acmod"} loaded-mods)
      (throw (RuntimeException. "Required mod 'acmod' is not loaded!")))))

(defn scan-dependencies! []
  (validate-dependencies!))