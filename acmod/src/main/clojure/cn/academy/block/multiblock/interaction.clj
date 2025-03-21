(ns cn.academy.block.multiblock.interaction
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.block.event :as event]
            [mcmod.block :as block]
            [mcmod.direction :as dir]))

(defprotocol IMultiblockInteraction
  "Protocol for interacting with multiblock structures"
  (can-interact? [this side] "Check if interaction is possible from side")
  (interact [this player side] "Handle player interaction from side"))

(defn handle-block-activation
  "Handle right-click on multiblock member"
  [world pos player hand]
  (when-let [block (block/get-tile-entity world pos)]
    (when (base/is-multiblock-part? block)
      (if-let [controller (base/get-controller block)]
        ;; Structure exists - try to interact
        (let [facing (base/get-block-facing block)]
          (when (and (satisfies? IMultiblockInteraction controller)
                    (can-interact? controller facing))
            (interact controller player facing)
            true))
        ;; No structure - try to form one
        (let [connected (base/find-connected-blocks world pos)]
          (when (> (count connected) 1)
            (doseq [member-pos connected
                    :let [member (block/get-tile-entity world member-pos)]]
              (when (and member (base/is-multiblock-master? member))
                ;; Found controller - try to form structure
                (when (base/validate-structure member world member-pos)
                  (base/on-structure-formed member)
                  (event/handle-structure-formed member connected)
                  true)))))))))

(defn handle-block-broken
  "Handle breaking a multiblock member"
  [world pos]
  (when-let [block (block/get-tile-entity world pos)]
    (when (base/is-multiblock-part? block)
      (when-let [controller (base/get-controller block)]
        (let [connected (base/find-connected-blocks world pos)]
          ;; Break the structure
          (base/on-structure-broken controller)
          (event/handle-structure-broken controller connected)
          ;; Clear controller references
          (doseq [member-pos connected
                  :let [member (block/get-tile-entity world member-pos)]]
            (when member
              (base/set-controller member nil)))))))))