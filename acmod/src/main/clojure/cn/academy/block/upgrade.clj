(ns cn.academy.block.upgrade
  (:require [cn.academy.block.validation :as validation]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; Upgrade definitions
(def upgrade-registry (atom {}))

(defn register-upgrade! [id upgrade-def]
  (swap! upgrade-registry assoc id upgrade-def))

;; Upgrade validation
(defn validate-upgrade! [block upgrade-def]
  (let [new-config ((:config-modifier upgrade-def) (:config block))]
    (validation/validate-config! new-config validation/machine-config)))

;; Default upgrades
(def machine-upgrades
  {"efficiency_upgrade"
   {:name "Efficiency Upgrade"
    :max-level 3
    :config-modifier (fn [config level]
                      (update config :base-efficiency #(* % (+ 1 (* level 0.25)))))
    :requirements {:energy-capacity 1000}}
   
   "capacity_upgrade"
   {:name "Capacity Upgrade"
    :max-level 4
    :config-modifier (fn [config level]
                      (update config :energy-capacity #(* % (+ 1 (* level 0.5)))))
    :requirements {:energy-capacity 0}}
   
   "speed_upgrade"
   {:name "Speed Upgrade"
    :max-level 2
    :config-modifier (fn [config level]
                      (-> config
                          (update :work-speed #(* % (+ 1 (* level 0.3))))
                          (update :energy-per-tick #(* % (+ 1 (* level 0.4))))))
    :requirements {:work-speed 0}}})

;; Apply upgrades
(defn can-apply-upgrade? [block upgrade-id]
  (error/with-safe-execution (:type block) :upgrade
    (when-let [upgrade (get @upgrade-registry upgrade-id)]
      (and (validation/validate-machine! block)
           (every? (fn [[k v]]
                    (>= (get-in block [:config k] 0) v))
                  (:requirements upgrade))))))

(defn apply-upgrade! [block upgrade-id]
  (error/with-safe-execution (:type block) :upgrade
    (when-let [upgrade (get @upgrade-registry upgrade-id)]
      (let [current-level (get-in block [:state :upgrades upgrade-id] 0)]
        (when (and (< current-level (:max-level upgrade))
                  (can-apply-upgrade? block upgrade-id))
          (let [new-level (inc current-level)
                new-config ((:config-modifier upgrade) (:config block) new-level)]
            (when (validate-upgrade! block upgrade)
              (swap! (:state block) assoc-in [:upgrades upgrade-id] new-level)
              (swap! block assoc :config new-config)
              true)))))))

;; Initialize upgrades
(defn init-upgrades! []
  (doseq [[id upgrade] machine-upgrades]
    (register-upgrade! id upgrade))
  true)