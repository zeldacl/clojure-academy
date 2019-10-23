(ns cn.li.mcmod.blocks
  (:require [cn.li.mcmod.utils :refer [with-prefix get-fullname construct update-map-keys gen-method ensure-registered]])
  (:import (net.minecraft.block Block Block$Properties)
           (net.minecraft.state IProperty BooleanProperty IntegerProperty))
  ;(:require (cn.li.mcmod.core :refer [defclass]))
  )


;(gen-class
;  :name "ttt"
;  :extends cn.li.mcmod.BaseBlock
;  ;:init ~'initialize
;  ;:constructors {[] [Block$Properties]}
;  )


(defn create-block-properties [properties]
  (let [block-properties (Block$Properties/create (:material properties))]
    ; use cond->
    (when-let [sound (:sound properties)]
      (.sound block-properties sound))
    (when-let [hardness (:hardness properties)]
      (.hardnessAndResistance block-properties hardness))
    ;(when-let [[harvest level] (:harvest-level properties)]
    ;  (.hardnessAndResistance block-properties harvest level))
    block-properties))

(defmulti create-state-property (fn [name property]
                                  (first property)))

(defmethod create-state-property :bool [name property]
  (BooleanProperty/create name))

(defmethod create-state-property :integer [name property]
  (IntegerProperty/create name (nth property 1) (nth property 2)))

(defn ->state-property [name property]
  (cond
    (instance? IProperty property) property
    (keyword? property) (create-state-property name [property])
    :else (create-state-property name property)))


(defn sanitize-state-properties [state-properties]
  ;(mapv (fn [[name property]] (->state-property name property)) state-properties)
  (reduce-kv #(assoc %1 %2 (->state-property (str %2) %3)) {} state-properties))

;(defmulti instance-block (fn [class-name & constructor-args]
;                           (keyword class-name)))

(defmacro defblock [block-name & args]
  (let [blockdata (apply hash-map args)
        class-name (symbol block-name)
        overrides (:overrides blockdata)
        ;container? (:container? blockdata)
        prefix (str block-name "-")
        registry-name (or (:registry-name blockdata) (str block-name))
        ;state-properties (sanitize-state-properties (:state-properties blockdata))
        ;options-map (dissoc options-map :events)
        name-ns (get blockdata :ns *ns*)
        fullname (get-fullname name-ns class-name)
        overrides (update-map-keys gen-method overrides)
        overrides (map (fn [override]
                         `(defn ~(key override) [~'this ~'& ~'args]
                            (apply ~(val override) ~'args))) overrides)
        ]
    ;(ensure-registered)
    ;`(do
    ;   (defclass ~block-name ~Block ~blockdata)
    ;   ~(if overrides
    ;      `(with-prefix ~(str block-name "-")
    ;         ~@overrides)))
    `(do
       (gen-class
         :name ~fullname
         :prefix ~(symbol prefix)
         :extends ~Block
         :init ~'initialize
         :constructors {[] [Block$Properties]}
         :post-init ~'post-initialize
         :state state
         )
       (def ~class-name ~fullname)
       (import ~fullname)
       ;(defmethod instance-block ~(keyword block-name) [~'class-name ~'& ~'constructor-args]
       ;  (apply construct ~class-name ~'constructor-args))
       (with-prefix ~prefix
         (defn ~'initialize
           ([~'& ~'args]
            [[(create-block-properties ~(:properties blockdata))] (atom {
                                                                         :state-properties (sanitize-state-properties ~(:state-properties blockdata))
                                                                         })]))
         (defn ~'post-initialize [~'obj ~'& ~'args]
           (.setRegistryName ~'obj ~registry-name))
         (defn ~'fillStateContainer [~'this ~'builder]
           ;(apply '.add ~'builder (mapv #(second %1) (:state-properties (deref ('.state ~'this)) ) ))
           )
         )
       ~(if overrides
          `(with-prefix ~(str block-name "-")
             ~@overrides))
       )
    ))


(defn instance-block [class-name & constructor-args]
  (apply construct class-name constructor-args))


;(let [a (doto
;          (Block$Properties/create Material/AIR)
;          (.hardnessAndResistance 0.5 0.5)
;          (.sound SoundType/WOOD)
;          )])
