(ns cn.academy.block.multiblock.interaction
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.api.block :as block-api])
  (:import [net.minecraft.util Direction]))

(defn find-connected-blocks
  "Find all blocks connected to the given position"
  [world pos]
  (loop [to-check #{pos}
         checked #{}
         connected #{}]
    (if (empty? to-check)
      connected
      (let [current (first to-check)
            current-te (block-api/get-tile-entity world current)]
        (if (and current-te 
                 (satisfies? base/IMultiblockMember current-te))
          (let [neighbors (for [dir (Direction/values)
                              :let [neighbor-pos (.offset current dir)
                                   neighbor-te (block-api/get-tile-entity 
                                               world neighbor-pos)]
                              :when (and neighbor-te
                                       (satisfies? base/IMultiblockMember 
                                                 neighbor-te)
                                       (base/can-connect? current-te 
                                                        neighbor-te))]
                          neighbor-pos)]
            (recur (into (disj to-check current) 
                        (remove checked neighbors))
                   (conj checked current)
                   (conj connected current)))
          (recur (disj to-check current)
                 (conj checked current)
                 connected))))))

(defn handle-block-activation
  "Handle right-click on multiblock member"
  [world pos player hand]
  (when-let [te (block-api/get-tile-entity world pos)]
    (when (satisfies? base/IMultiblockMember te)
      (if-let [controller (base/get-controller te)]
        ;; Open GUI if structure complete
        (when (base/is-complete? controller)
          (block-api/open-gui world pos player))
        ;; Try to form structure
        (let [connected (find-connected-blocks world pos)]
          (when (> (count connected) 1)
            (doseq [block-pos connected
                    :let [member (block-api/get-tile-entity world 
                                                           block-pos)]]
              (when (= "controller" (base/get-member-type member))
                (doseq [member-pos connected
                        :let [member-te (block-api/get-tile-entity 
                                       world member-pos)]]
                  (base/set-controller member-te member))))))))))

(defn handle-block-broken
  "Handle multiblock member being broken"
  [world pos]
  (when-let [te (block-api/get-tile-entity world pos)]
    (when (satisfies? base/IMultiblockMember te)
      (when-let [controller (base/get-controller te)]
        ;; Disconnect all members
        (doseq [member-pos (find-connected-blocks world pos)
                :let [member (block-api/get-tile-entity world 
                                                       member-pos)]]
          (base/set-controller member nil))))))