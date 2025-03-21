(ns cn.academy.core.init
  (:require [mcmod.protocols :refer [ILifecycle]]
            [cn.academy.block.multiblock.core :as multiblock]
            [cn.academy.block.machine.core :as machine]
            [cn.academy.block.resource.core :as resource]
            [cn.academy.block.render.core :as render]
            [cn.academy.block.error :as error]
            [cn.academy.block.event :as event]
            [cn.academy.block.api :as api]
            [clojure.tools.logging :as log]))

;; System initialization order
(def init-order
  [{:id :diagnostics
    :start init-diagnostics!
    :desc "Diagnostics system"}
   
   {:id :monitoring
    :start init-monitoring!
    :desc "Monitoring system"}
   
   {:id :error
    :start error/init-error-system!
    :desc "Error handling system"}
   
   {:id :event
    :start event/register-standard-handlers!
    :desc "Event system"}
   
   {:id :render
    :start render/init-render-system!
    :desc "Rendering system"}
   
   {:id :resource
    :start resource/init-resource-system!
    :desc "Resource system"}
   
   {:id :machine
    :start machine/init-machines!
    :desc "Machine system"}
   
   {:id :multiblock
    :start multiblock/init-multiblock!
    :desc "Multiblock system"}
   
   {:id :api
    :start api/init-api!
    :desc "Public API"}])

;; Diagnostics initialization
(defn init-diagnostics! []
  (log/info "Initializing diagnostics system")
  ;; Initialize performance monitoring
  (doseq [[category threshold] 
          {"energy-transfer" 50
           "network-update" 100
           "world-tick" 50
           "render-update" 16}]
    (monitoring/set-threshold! category threshold))
  
  ;; Start profiling key subsystems
  (doseq [category ["energy" "network" "world" "render"]]
    (profiling/start-profiling! category))
    
  ;; Schedule periodic diagnostic reports
  (future
    (try
      (while true
        (Thread/sleep (* 30 60 1000)) ; Every 30 minutes
        (diagnostics/write-report! "logs/academy-diagnostics.log"))
      (catch InterruptedException _)))
  true)

;; Performance monitoring initialization  
(defn init-monitoring! []
  (log/info "Initializing monitoring system")
  (monitoring/reset-metrics!)
  (monitoring/start-metrics-collection!)
  true)

;; Initialization
(defn init-subsystem!
  "Initialize a single subsystem"
  [{:keys [id start desc]}]
  (log/info "Initializing" desc)
  (try 
    (start)
    (log/info desc "initialized successfully")
    true
    (catch Exception e
      (log/error "Failed to initialize" desc ":" (.getMessage e))
      false)))

(defn init-all!
  "Initialize all subsystems in order"
  []
  (log/info "Starting AcademyCraft initialization")
  (let [results (map init-subsystem! init-order)
        success (every? true? results)]
    (if success
      (do
        (log/info "AcademyCraft initialized successfully")  
        (diagnostics/write-startup-report!))
      (log/error "AcademyCraft initialization failed"))
    success))

;; Lifecycle implementation  
(extend-type cn.academy.core.init
  ILifecycle
  (start [_]
    (init-all!))
  
  (stop [_]
    (log/info "Shutting down AcademyCraft")
    (profiling/stop-all-profiling!)
    (monitoring/stop-metrics-collection!)
    (diagnostics/write-shutdown-report!)))

;; Register for lifecycle management
(mcmod.lifecycle/register-lifecycle *ns*)