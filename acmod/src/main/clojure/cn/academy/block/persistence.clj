(ns cn.academy.block.persistence
  (:require [cn.academy.block.serialization :as serial]
            [cn.academy.block.error :as error]
            [cn.academy.block.env :as env]
            [clojure.tools.logging :as log]))

;; Data versioning
(def current-data-version 1)

(defprotocol IVersioned
  (get-version [this])
  (migrate-data [this target-version]))

;; Data migration handlers
(def migration-handlers
  {0 (fn [data]
       ;; Migration from version 0 to 1
       (-> data
           (assoc :version 1)
           (update :state #(merge % {:upgrades {}}))))})

;; Data migration
(defn migrate-block-data [data target-version]
  (loop [current-data data
         current-version (or (:version current-data) 0)]
    (if (= current-version target-version)
      current-data
      (if-let [handler (get migration-handlers current-version)]
        (recur (handler current-data) (inc current-version))
        (throw (Exception. (str "No migration handler for version " current-version)))))))

;; Block data persistence
(defn save-block-data! [block]
  (error/with-safe-execution (:id block) :persistence
    (let [data (serial/write-to-nbt block)]
      (mcmod.block/write-nbt! block data)
      true)))

(defn load-block-data! [block nbt]
  (error/with-safe-execution (:id block) :persistence
    (let [data (serial/read-from-nbt block nbt)
          migrated (migrate-block-data data current-data-version)]
      (reset! (:state block) (:state migrated))
      (when (not= (:version data) current-data-version)
        (save-block-data! block))
      true)))

;; Periodic saving
(defn start-autosave! []
  (let [interval (env/get-config [:autosave :interval] 300)]
    (mcmod.scheduler/schedule-periodic
      interval
      (fn []
        (doseq [block (mcmod.block/get-loaded-blocks)]
          (save-block-data! block))))))

;; World unload handling
(defn handle-world-unload! [world]
  (doseq [block (mcmod.block/get-blocks-in-world world)]
    (save-block-data! block)))

;; Block chunk load/unload handling
(defn handle-chunk-load! [chunk]
  (doseq [block (mcmod.block/get-blocks-in-chunk chunk)]
    (when-let [nbt (mcmod.block/read-nbt block)]
      (load-block-data! block nbt))))

(defn handle-chunk-unload! [chunk]
  (doseq [block (mcmod.block/get-blocks-in-chunk chunk)]
    (save-block-data! block)))

;; Register event handlers
(defn init-persistence! []
  (mcmod.events/register-handler! :world-unload handle-world-unload!)
  (mcmod.events/register-handler! :chunk-load handle-chunk-load!)
  (mcmod.events/register-handler! :chunk-unload handle-chunk-unload!)
  (start-autosave!))