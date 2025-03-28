(ns cn.academy.block.behavior
  (:require [clojure.tools.logging :as log]))

;; Common block behaviors that can be reused across different block types
(def common-behaviors
  {:machine {:on-placed (fn [world pos state player]
                         (when-let [tile (cm.mcmod.block/get-tile-entity world pos)]
                           (cm.mcmod.block/mark-dirty! tile)))

             :on-broken (fn [world pos]
                         (when-let [tile (mcmod.block/get-tile-entity world pos)]
                           (mcmod.block/cleanup-tile! tile)))

             :on-activated (fn [world pos state player hand]
                           (when-not (.isSneaking player)
                             (mcmod.block/open-gui! player world pos)
                             true))}

   :energy-producer (merge
                     {:check-power-connections (fn [world pos]
                                               (mcmod.energy/scan-network world pos))
                      :update-energy-state! (fn [tile amount]
                                            (mcmod.energy/update-energy! tile amount))}
                     (:machine common-behaviors))

   :multiblock-member {:on-neighbor-changed (fn [world pos block neighbor-pos]
                                            (mcmod.multiblock/check-structure world pos))
                      :validate-placement (fn [world pos]
                                          (mcmod.multiblock/can-place? world pos))}})

(defn get-behavior [block-type behavior-key]
  (get-in common-behaviors [block-type behavior-key]))

(defn apply-behavior! [block-type behavior-key & args]
  (when-let [behavior-fn (get-behavior block-type behavior-key)]
    (apply behavior-fn args)))

(defn combine-behaviors [& types]
  (fn [behavior-key & args]
    (some #(apply apply-behavior! % behavior-key args) types)))
