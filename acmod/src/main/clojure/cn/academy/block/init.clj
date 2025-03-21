(ns cn.academy.block.init
  (:require [cn.academy.block.error :as error]
            [cn.academy.block.network.core :as network]
            [cn.academy.block.gui :as gui]
            [cn.academy.block.recipe :as recipe]
            [cn.academy.block.machine :as machine]
            [cn.academy.block.container :as container]
            [cn.academy.block.capability :as cap]
            [cn.academy.block.particle :as particle]
            [cn.academy.block.fluid :as fluid]
            [cn.academy.block.scheduler :as scheduler]
            [cn.academy.block.tile-entity :as te]
            [cn.academy.block.upgrade :as upgrade]
            [cn.academy.block.world :as world]
            [cn.academy.block.storage :as storage]
            [cn.academy.block.hologram :as hologram]
            [cn.academy.block.lang :as lang]
            [mcmod.protocols :refer [IModInitializer]]
            [clojure.tools.logging :as log]))

;; Module initialization order
(def module-order
  [:error
   :scheduler
   :world
   :storage
   :capability
   :network
   :fluid
   :recipe
   :machine
   :container
   :tile-entity
   :upgrade
   :particle
   :hologram
   :gui
   :lang])

;; Initialize handlers map
(def init-handlers
  {:error error/init-errors!
   :scheduler scheduler/init-scheduler!
   :world world/init-world!
   :storage storage/init-storage!
   :capability cap/init-capabilities!
   :network network/init-network!
   :fluid fluid/init-fluids!
   :recipe recipe/init-recipes!
   :machine machine/init-machines!
   :container container/init-containers!
   :tile-entity te/init-tile-entities!
   :upgrade upgrade/init-upgrades!
   :particle particle/init-particles!
   :hologram hologram/init-holograms!
   :gui gui/init-gui!
   :lang lang/init-lang!})

;; Mod initializer implementation
(defrecord ModInitializer [providers state-atom]
  IModInitializer
  (pre-init! [_]
    (log/info "Starting pre-initialization phase")
    (doseq [module module-order]
      (error/with-error-handling :init
        (when-let [handler (get init-handlers module)]
          (log/debug "Initializing module:" (name module))
          (handler)))))
  
  (init! [_]
    (log/info "Starting initialization phase")
    (doseq [[provider-type provider] providers]
      (error/with-error-handling :init
        (log/debug "Initializing provider:" (name provider-type))
        (.initialize! provider))))
  
  (post-init! [_]
    (log/info "Starting post-initialization phase")
    (swap! state-atom assoc :initialized true))
  
  (is-initialized? [_]
    (:initialized @state-atom)))

;; Factory function
(defn create-initializer [providers]
  (->ModInitializer providers (atom {:initialized false})))

;; Module dependency handling
(def module-dependencies
  {:gui [:container :lang]
   :machine [:capability :storage]
   :particle [:scheduler]
   :hologram [:particle]
   :recipe [:storage]
   :tile-entity [:capability :machine]})

(defn validate-dependencies! []
  (doseq [[module deps] module-dependencies]
    (when-not (every? #(contains? init-handlers %) deps)
      (throw (ex-info (str "Missing dependency for " (name module))
                     {:module module
                      :missing-deps (remove #(contains? init-handlers %) deps)})))))

;; Initialization helpers
(defn init-module! [module]
  (when-let [handler (get init-handlers module)]
    (error/with-error-handling :init
      (handler))))

(defn init-all! [initializer]
  (validate-dependencies!)
  (.pre-init! initializer)
  (.init! initializer)
  (.post-init! initializer))

;; System shutdown
(defn shutdown! []
  (log/info "Shutting down mod systems")
  (scheduler/cancel-task! "system-monitor")
  (error/clear-old-errors!)
  (world/init-world!))