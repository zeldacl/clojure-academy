(ns cn.li.academy.energy.tileentities
  (:require [cn.li.academy.core.tileentities :refer [deftileinventory]]
            [cn.li.academy.energy.hepler :refer [get-node-bandwidth-by-id get-node-max-energy-by-id pull-enrgy-from-stack charge-enrgy-to-stack]]
            [forge-clj.util :refer [with-prefix remote?]])
  (:use [forge-clj.tileentity :only [deftileentity]])
  (:import (net.minecraft.inventory IInventory)
           (net.minecraft.tileentity TileEntity)))

(defn get-entity-max-energy [entity]
  (get-node-max-energy-by-id (.getBlockMetadata entity)))

(defn update-charge-in [entity]
  (let [stack (.getStackInSlot entity 0)]
    (if (and stack)                                         ;todo: add support check
      (let [node-type-id (.getBlockMetadata entity)
            bandwidth (get-node-bandwidth-by-id node-type-id)
            max-energy (get-node-max-energy-by-id node-type-id)
            energy (:energy entity)
            req (min bandwidth (- max-energy energy))
            pull (pull-enrgy-from-stack stack req false)]
        (assoc! entity :charging-in (not= pull 0))
        (assoc! entity :energy (+ energy pull)))
      (assoc! entity :charging-in false))))

(defn update-charge-out [entity]
  (let [stack (.getStackInSlot entity 1)]
    (if (and stack)                                         ;todo
      (let [energy (:energy entity)]
        (when (> energy 0)
          (let [node-type-id (.getBlockMetadata entity)
                bandwidth (get-node-bandwidth-by-id node-type-id)
                cur (min bandwidth energy)
                left (charge-enrgy-to-stack stack cur false)]
            (assoc! entity :charging-out (not= cur left))
            (assoc! entity :energy (- energy (- cur left))))))
      (assoc! entity :charging-out false))))


(deftileinventory node-inventory-entity "wireless_node" 2
               :fields {:energy 0
                        :update-ticker 0
                        :enabled false
                        :charging-in false
                        :charging-out false
                        :name "Unnamed"})

(with-prefix node-inventory-entity-
             (defn updateEntity [^TileEntity this]
               (when (remote? (.getWorldObj this))
                 (update-charge-in this)
                 (update-charge-out this))))


(defn new-node-entity [& args]
  (.newInstance ^Class node-inventory-entity))
