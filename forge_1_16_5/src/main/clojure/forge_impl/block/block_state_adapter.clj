(ns forge-impl.block.block-state-adapter
  (:require [mcmod.protocols :refer :all]
            [mcmod.block.block-state :as block-state])
  (:import [net.minecraft.state StateContainer StateContainer$Builder]
           [net.minecraft.state.properties BooleanProperty IntegerProperty]
           [net.minecraft.block Block]
           [net.minecraft.block.material Material]
           [net.minecraft.state IProperty]))

;; Convert mcmod property to Forge property
(defmulti create-forge-property 
  (fn [prop] (type prop)))

(defmethod create-forge-property block-state.BooleanProperty [prop]
  (BooleanProperty/create (get-name prop)))

(defmethod create-forge-property block-state.IntegerProperty [prop]
  (IntegerProperty/create (get-name prop) 
                         (:min prop) 
                         (:max prop)))

;; Create state container for a block
(defn create-state-container [^Block block props]
  (let [builder (StateContainer$Builder. block)]
    (doseq [[_ prop] props]
      (.add builder (create-forge-property prop)))
    (.build builder)))

;; Convert mcmod state value to Forge state
(defn convert-state-value [^IProperty prop value]
  (cond
    (instance? BooleanProperty prop) (boolean value)
    (instance? IntegerProperty prop) (int value)
    :else value))

;; Convert mcmod block state to Forge block state
(defn convert-to-forge-state [forge-state mcmod-state props]
  (reduce (fn [state [name value]]
            (if-let [prop (get props name)]
              (.with state prop (convert-state-value prop value))
              state))
          forge-state
          (:values mcmod-state)))

;; Create default state 
(defn create-default-state [container mcmod-block]
  (let [default-state (.getBaseState container)
        mcmod-state (get-default-state mcmod-block)
        props (.getProperties container)]
    (convert-to-forge-state default-state mcmod-state props)))

;; Update state based on world
(defn update-state-from-world [forge-state mcmod-block world pos]
  (let [mcmod-state (get-actual-state mcmod-block world pos forge-state)
        props (.getProperties forge-state)]
    (convert-to-forge-state forge-state mcmod-state props)))