(ns cn.academy.core.util.version-diagnostics
  (:require [cn.academy.core.util.logging :refer [log-debug log-warn]]
            [cn.academy.core.util.diagnostics :as diag])
  (:import [net.minecraftforge.fml.common Loader]))

(defn get-forge-version []
  (try
    (.getVersion (Class/forName "net.minecraftforge.common.ForgeVersion"))
    (catch Exception _
      "Unknown")))

(defn get-mod-list []
  (try
    (for [mod-container (.getActiveModList (Loader/instance))]
      {:id (.getModId mod-container)
       :version (.getVersion mod-container)
       :name (.getName mod-container)})
    (catch Exception _
      [])))

(defn check-version-compatibility []
  (let [forge-version (get-forge-version)
        mods (get-mod-list)]
    (doseq [mod mods]
      (when (and (.startsWith (:id mod) "forge")
                 (not= forge-version (:version mod)))
        (log-warn "Forge version mismatch detected:"
                 "expected" forge-version
                 "but found" (:version mod))))
    {:forge-version forge-version
     :mods mods}))

(defn record-version-info! []
  (let [version-info (check-version-compatibility)]
    (diag/record-diagnostic-event!
      :version-check
      version-info)
    version-info))

(defn check-api-compatibility []
  (let [api-classes
        ["net.minecraftforge.energy.IEnergyStorage"
         "net.minecraftforge.energy.CapabilityEnergy"
         "net.minecraft.tileentity.TileEntity"
         "net.minecraft.util.math.BlockPos"]
        missing (filter
                 (fn [class-name]
                   (try
                     (Class/forName class-name)
                     false
                     (catch ClassNotFoundException _
                       true)))
                 api-classes)]
    (when (seq missing)
      (log-warn "Missing required API classes:" (pr-str missing)))
    {:compatible? (empty? missing)
     :missing-classes missing}))

(defn validate-runtime-environment! []
  (let [version-info (record-version-info!)
        api-check (check-api-compatibility)]
    (when-not (:compatible? api-check)
      (log-warn "Runtime environment validation failed"))
    {:version version-info
     :api api-check}))