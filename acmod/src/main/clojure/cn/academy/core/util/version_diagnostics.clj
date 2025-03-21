(ns cn.academy.core.util.version-diagnostics
  (:require [cn.academy.core.util.logging :refer [log-debug log-warn]]
            [cn.academy.core.util.diagnostics :as diag]
            [mcmod.core :as mccore]))

(defn get-forge-version []
  (mccore/get-forge-version))

(defn get-mod-list []
  (try
    (mccore/get-active-mods)
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
        ["energy-storage" ; Abstracted reference to IEnergyStorage
         "energy-capability" ; Abstracted reference to CapabilityEnergy
         "tile-entity" ; Abstracted reference to TileEntity
         "block-position"] ; Abstracted reference to BlockPos
        missing (filter
                 (fn [api-name]
                   (not (mccore/is-api-available? api-name)))
                 api-classes)]
    (when (seq missing)
      (log-warn "Missing required API classes:" (pr-str missing)))
    {:compatible? (empty? missing)
     :missing-apis missing}))

(defn validate-runtime-environment! []
  (let [version-info (record-version-info!)
        api-check (check-api-compatibility)]
    (when-not (:compatible? api-check)
      (log-warn "Runtime environment validation failed"))
    {:version version-info
     :api api-check}))