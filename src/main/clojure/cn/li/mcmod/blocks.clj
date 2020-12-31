(ns cn.li.mcmod.blocks
  (:require [cn.li.mcmod.utils :refer [with-prefix get-fullname construct update-map-keys gen-method ensure-registered]]
            [clojure.tools.logging :as log]
            ;[cn.li.mcmod.registry :refer [set-registry-name]]
            [clojure.string :as str])
  (:import (net.minecraft.block Block Block$Properties)
           (net.minecraft.state IProperty BooleanProperty IntegerProperty)
           (net.minecraft.inventory.container Container)
           (net.minecraftforge.common.extensions IForgeContainerType))
  ;(:require (cn.li.mcmod.core :refer [defclass]))
  )


;(gen-class
;  :name "ttt"
;  :extends cn.li.mcmod.BaseBlock
;  ;:init ~'initialize
;  ;:constructors {[] [Block$Properties]}
;  )

;(ReflectionHelper)


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

(def ^:dynamic *block-states* (atom {}))

(defn update-block-states [name property]
  (swap! *block-states* assoc (keyword name) property))

(defn get-block-states [name]
  (get @*block-states* (keyword name)))


(defmacro defblockstate [name property]
  `(let [p# (create-state-property ~(str/replace (str name) "-" "_") ~property)]
     (update-block-states ~(str name) p#)
     (def ~name p#)))


;(defn getfield
;  [this key]
;  ((.-state @this) key))

(defmacro defblock [block-name & args]
  (let [blockdata (apply hash-map args)
        class-name (symbol block-name)
        overrides (:overrides blockdata)
        ;container? (:container? blockdata)
        prefix (str block-name "-")
        ;registry-name (or (:registry-name blockdata) (str block-name))
        registry-name (:registry-name blockdata)
        ;state-properties (sanitize-state-properties (:state-properties blockdata))
        ;options-map (dissoc options-map :events)
        name-ns (get blockdata :ns *ns*)
        fullname (get-fullname name-ns class-name)
        this-sym (with-meta 'this {:tag fullname})
        overrides (update-map-keys gen-method overrides)
        overrides (map (fn [override]
                         `(defn ~(key override) [~'this ~'& ~'args]
                            (apply ~(val override) ~'args))) overrides)
        ;state-property-names (mapv #(.getName ^IProperty %1) (:state-properties blockdata))
        ;state-properties (atom (sanitize-state-properties (:state-properties blockdata)))
        ;get-all-state-properties (constantly state-properties)
        ;state-keys (map)
        x (gensym)
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
         :state ~'state
         )
       ;(comment (compile ~name-ns))
       (def ~class-name ~fullname)
       ;(def ~class-name (eval '~fullname))
       (import ~fullname)
       ;(defmethod instance-block ~(keyword block-name) [~'class-name ~'& ~'constructor-args]
       ;  (apply construct ~class-name ~'constructor-args))
       (with-prefix ~prefix
         (defn ~'initialize
           ([~'& ~'args]
            [[(create-block-properties ~(:properties blockdata))] (atom {
                                                                         ;:state-properties (sanitize-state-properties ~(:state-properties blockdata))
                                                                         })]))
         (defn ~'post-initialize [~(with-meta x {:tag `Block}) ~'& ~'args]
           ;(log/info "^^^^^^^^^^^^^^^^^^" ~x ~'args)
           ~(when registry-name
              ;`(.setRegistryName ~x ~registry-name)
              ;`(set-registry-name ~x ~registry-name)
              )
           ;(.setRegistryName ~'obj ~registry-name)
           )
         (defn ~'fillStateContainer [~'this ~'builder]
           ;(log/info ~'this ~'builder)
           ;(log/info "qqqq" *block-states* "eeeee" ~(:state-properties blockdata))
           ;(apply '.add ~'builder (mapv #(second %1) @~state-properties ))
           ;(log/info "2222" '.add "3333" ~'builder "4444" (mapv #(get-block-states %1) ~(:state-properties blockdata)))
           ;(map #('.add ~'builder (get-block-states %1)) ~state-property-names)
           ;(.add ~'builder (into-array IProperty (mapv #(get-block-states %1) ~state-property-names)))
           (.add ~'builder (into-array IProperty ~(:state-properties blockdata)))
           ;(apply '.add ~'builder (mapv #(get-block-states %1) ~(:state-properties blockdata) ))
           )
         ;(let [m# (~sanitize-state-properties (:state-properties ~blockdata))]
         ;  (log/info "rrrr" m#)
         ;  (log/info "yyyy" (mapv #(second %1) m# ))
         ;  ;(apply '.add ~'builder (mapv #(second %1) m# )))
         ;
         ;;(apply '.add ~'builder (mapv #(second %1) (getfield ~'this :state-properties) ))
         ;;(apply '.add ~'builder (mapv #(second %1) (:state-properties (deref (.-state ~this-sym) ) ) ))
         ;)
         )
       ~(if overrides
          `(with-prefix ~(str block-name "-")
             ~@overrides))
       )
    ))


(defn instance-block [class-name & constructor-args]
  (apply construct class-name constructor-args)
  ;(apply construct (resolve class-name) constructor-args)
  )

(defn instance-tile-entity [class-name & constructor-args]
  (apply construct class-name constructor-args)
  ;(apply construct (resolve class-name) constructor-args)
  )


