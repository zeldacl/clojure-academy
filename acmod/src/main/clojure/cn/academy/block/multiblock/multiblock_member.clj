(ns cn.academy.block.multiblock.multiblock-member
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.block.core :as core]
            [cn.academy.api.block :as block-api]))

(defprotocol IMultiblockMemberData
  (get-valid-connections [this] "Get valid connection types")
  (get-member-type [this] "Get member block type"))

(defrecord MultiblockMember [state-atom member-data]
  base/IMultiblockMember
  (get-controller [this]
    (:controller @state-atom))
  
  (set-controller [this controller]
    (swap! state-atom assoc :controller controller)
    (block-api/mark-dirty! this))
  
  (can-connect? [this other]
    (let [valid-types (get-valid-connections member-data)]
      (when-let [other-type (get-member-type (:member-data other))]
        (contains? valid-types other-type))))
  
  (on-connection [this other]
    ;; Handle any special connection logic
    nil)
  
  (get-multiblock-data [this]
    member-data)

  core/IBlockEntity
  (load-data [this data]
    (reset! state-atom {:controller (:controller data)}))
  
  (save-data [this]
    {:controller (:controller @state-atom)})
  
  (get-capabilities [this]
    ;; Return capabilities if part of complete structure
    (when-let [controller (get-controller this)]
      (when (base/is-complete? controller)
        (:capabilities @state-atom)))))