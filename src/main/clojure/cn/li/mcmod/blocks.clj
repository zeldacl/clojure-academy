(ns cn.li.mcmod.blocks
  (:require [cn.li.mcmod.utils :refer [with-prefix get-fullname construct update-map-keys gen-method ensure-registered]])
  (:import (net.minecraft.block Block Block$Properties))
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
    (when-let [sound (:sound properties)]
      (.sound block-properties sound))
    (when-let [hardness (:hardness properties)]
      (.hardnessAndResistance block-properties hardness))
    (when-let [harvest-level (:harvest-level properties)]
      (.harvestLevel block-properties harvest-level))))

(defmacro defblock [block-name & args]
  (let [blockdata (apply hash-map args)
        overrides (:overrides blockdata)
        ;container? (:container? blockdata)
        prefix (str block-name "-")
        ;options-map (dissoc options-map :events)
        name-ns (get blockdata :ns *ns*)
        fullname (get-fullname name-ns block-name)
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
         ;:constructors {[] [Block$Properties]}
         )
       (with-prefix ~prefix
         (defn ~'initialize
           ([~'& ~'args]
            [[(create-block-properties ~(:properties blockdata))] (atom {})])))
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
