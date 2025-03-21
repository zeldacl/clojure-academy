(ns cn.academy.block.multiblock.interaction
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [mcmod.direction :as dir]))

(defprotocol IMultiblockInteraction
  "Protocol for interacting with multiblock structures"
  (can-interact? [this side] "Check if interaction is possible from side")
  (interact [this player side] "Handle player interaction from side")
  (get-interaction-type [this side] "Get type of interaction available"))

(defn get-interaction-handler
  "Get interaction handler for a multiblock part"
  [block side]
  (when-let [controller (base/get-controller block)]
    (when (satisfies? IMultiblockInteraction controller)
      (when (can-interact? controller (dir/from-index side))
        controller))))

(defn handle-interaction
  "Handle player interaction with multiblock structure"
  [block player side]
  (when-let [handler (get-interaction-handler block side)]
    (interact handler player (dir/from-index side))))

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