(ns cn.academy.block.multiblock.multiblock-member
  (:require [cn.academy.protocols.multiblock :as mb]
            [cn.academy.protocols.block :as block-api]
            [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.block.core :as core]
            [mcmod.protocols :refer [IBlockEntity]]))

(defrecord MultiblockMember [state-atom member-type]
  base/IMultiblockMember
  (get-controller [_]
    (:controller @state-atom))
  
  (set-controller [this controller]
    (swap! state-atom assoc :controller controller)
    (block-api/mark-dirty! this))
  
  (can-connect? [_ other]
    (contains? (:valid-connections @state-atom) 
              (base/get-member-type other)))
  
  (get-member-type [_]
    member-type)
  
  (on-connection [this other]
    (when-let [controller (get-controller other)]
      (set-controller this controller)))

  IBlockEntity 
  (load-data [_ data]
    (reset! state-atom (merge {:valid-connections #{}} data)))
  
  (save-data [_]
    @state-atom)
  
  (get-capabilities [this]
    (when-let [controller (get-controller this)]
      (when (mcmod.protocols/is-complete? controller)
        (:capabilities @state-atom)))))

(defn create-member
  "Create a new multiblock member"
  [member-type & {:keys [valid-connections capabilities]}]
  (->MultiblockMember 
    (atom {:controller nil
           :valid-connections (or valid-connections #{})
           :capabilities (or capabilities {})})
    member-type))

;; Member registration
(def member-types
  {"controller" #{:casing :energy-port}
   "casing" #{:controller :casing :energy-port}
   "energy-port" #{:controller :casing}})

(defn get-valid-connections
  "Get valid connection types for a member type"
  [member-type]
  (get member-types member-type #{}))