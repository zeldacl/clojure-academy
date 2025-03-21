(ns cn.academy.block.core
  (:require [cn.academy.block.blocks :as blocks]
            [cn.academy.block.registry.init :as registry]
            [cn.academy.block.network.core :as network]
            [cn.academy.block.gui.core :as gui]
            [cn.academy.block.recipe.core :as recipe]
            [cn.academy.block.upgrade :as upgrade]
            [cn.academy.block.debug :as debug]
            [clojure.tools.logging :as log]))

(defprotocol IBlock
  "Core block functionality"
  (get-position [this] "Get block position")
  (get-properties [this] "Get block properties")
  (on-placed [this pos placer data] "Handle block placement")
  (on-removed [this pos] "Handle block removal")
  (on-activated [this pos activator data] "Handle block activation")
  (can-place? [this pos] "Check if block can be placed")
  (can-remove? [this pos] "Check if block can be removed"))

(defprotocol IBlockEntity
  "Block entity (tile entity) functionality"
  (load-data [this data] "Load block entity data")
  (save-data [this] "Save block entity data")
  (get-capabilities [this] "Get block capabilities")
  (mark-dirty [this] "Mark block as needing save")
  (on-load [this] "Called when block entity loads")
  (on-unload [this] "Called when block entity unloads"))

(defprotocol IBlockProperties
  "Block properties"
  (get-hardness [this] "Get block hardness")
  (get-resistance [this] "Get block blast resistance")
  (get-light-level [this] "Get block light emission level")
  (is-opaque? [this] "Check if block is opaque")
  (get-material [this] "Get block material type"))

(defrecord BlockProperties [hardness resistance light-level opaque? material]
  IBlockProperties
  (get-hardness [_] hardness)
  (get-resistance [_] resistance) 
  (get-light-level [_] light-level)
  (is-opaque? [_] opaque?)
  (get-material [_] material))

(defn create-properties
  "Create block properties"
  [& {:keys [hardness resistance light-level opaque? material]
      :or {hardness 3.0
           resistance 3.0
           light-level 0
           opaque? true
           material :stone}}]
  (->BlockProperties hardness resistance light-level opaque? material))

;; System initialization state
(def init-state (atom {:initialized false
                      :subsystems {}}))

;; Initialization order
(def subsystem-order
  [:registry
   :blocks
   :network
   :gui
   :recipe
   :upgrade])

;; Initialization functions
(defn init-subsystem! [system-key]
  (let [result
        (case system-key
          :registry (registry/init!)
          :blocks (blocks/init-registry!)
          :network (network/init-networking!)
          :gui (gui/init-guis!)
          :recipe (recipe/init-recipes!)
          :upgrade (upgrade/init-upgrades!))]
    (swap! init-state assoc-in [:subsystems system-key] result)
    result))

(defn init-all! []
  (try
    (when-not (:initialized @init-state)
      (debug/enable-debug!)
      (doseq [system subsystem-order]
        (log/info "Initializing subsystem:" system)
        (when-not (init-subsystem! system)
          (throw (Exception. (str "Failed to initialize " system)))))
      (swap! init-state assoc :initialized true)
      (log/info "Academy block systems initialized successfully")
      true)
    (catch Exception e
      (log/error "Failed to initialize Academy block systems:" (.getMessage e))
      false)))

;; Shutdown handling
(defn shutdown! []
  (when (:initialized @init-state)
    (try
      (debug/disable-debug!)
      (reset! init-state {:initialized false
                         :subsystems {}})
      (log/info "Academy block systems shutdown complete")
      true
      (catch Exception e
        (log/error "Error during shutdown:" (.getMessage e))
        false))))

;; System status
(defn system-status []
  {:initialized (:initialized @init-state)
   :subsystems (:subsystems @init-state)})