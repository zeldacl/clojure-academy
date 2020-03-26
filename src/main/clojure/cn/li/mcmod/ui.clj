(ns cn.li.mcmod.ui
  (:require [cn.li.mcmod.utils :refer [defclass get-fullname with-prefix construct]]
            [clojure.tools.logging :as log]
    ;[cn.li.mcmod.core :refer [defclass]]
            [clojure.string :as str])
  (:import (net.minecraftforge.common.extensions IForgeContainerType)
           (net.minecraft.inventory.container Container Slot)
           (net.minecraft.entity.player PlayerInventory)
           (net.minecraft.network PacketBuffer)
           (net.minecraftforge.items SlotItemHandler IItemHandler)
           (net.minecraftforge.items.wrapper InvWrapper)
           (net.minecraftforge.fml.network IContainerFactory)
           (net.minecraft.util ResourceLocation)
    ;(net.minecraft.util.text ITextComponent)
    ;(net.minecraft.client.gui.screen.inventory ContainerScreen)
    ;(com.mojang.blaze3d.platform GlStateManager GlStateManager$SourceFactor GlStateManager$DestFactor)
    ;(net.minecraftforge.api.distmarker OnlyIn Dist)
           ))

;;; This fn allows calling any method, as long as it's the first with that name in getDeclaredMethods().
;;; Works even when the arguments are primitive types.
;
;(defn call-method
;  [obj method-name & args]
;  (let [m (first (filter (fn [x] (.. x getName (equals method-name)))
;                         (.. obj getClass getDeclaredMethods)))]
;    (. m (setAccessible true))
;    (. m (invoke obj (into-array Object args)))))
;
;
;;; This function comes from clojure.contrib.reflect. A version of it is also in https://github.com/arohner/clj-wallhack.
;;; It allows calling any method whose arguments have class types, but not primitive types.
;
;(defn call-method
;  "Calls a private or protected method.
;   params is a vector of classes which correspond to the arguments to the method
;   obj is nil for static methods, the instance object otherwise.
;   The method-name is given a symbol or a keyword (something Named)."
;  [klass method-name params obj & args]
;  (-> klass (.getDeclaredMethod (name method-name)
;                                (into-array Class params))
;      (doto (.setAccessible true))
;      (.invoke obj (into-array Object args))))

(defmacro defcontainerslot [name & args]
  (let [classdata (apply hash-map args)]
    `(do
       (defclass ~name SlotItemHandler ~classdata))))

(defcontainerslot slot-inv)


;(defmulti transfer-slot? (fn [from to]
;                           [(class from) (class to)]))
;
;(defmethod transfer-slot? :default [from to] true)

(defn add-range
  [^net.minecraft.inventory.container.Container container ^IItemHandler handler index x y amount dx]
  (let [index (int index)
        x (int x)
        y (int y)
        amount (int amount)
        dx (int dx)
        add-slot (fn [^long i ^long x ^long y]
                   (let [^Container container container]
                     ; fixme protected method
                     ;(def a (ref 0))
                     ; (def klass (class a))
                     ; (def m (.getDeclaredMethod klass "currentVal" (into-array Class [])))
                     ; (.setAccessible m true)
                     ; (.invoke m a (into-array []))
                     (.addSlot ^Container container ^Slot (construct slot-inv handler i x y)))
                   )]
    (dorun (map #(add-slot (+ %1 index), (+ (* %1 dx) x), y)) (range amount)))
  ;([^Container container ^IItemHandler player-inventory]
  ; (add-range container player-inventory 0 6 163 9 18))
  )

(defn add-box [^Container container ^IItemHandler handler index x y x-amount y-amount dx dy]
  (dorun (map #(add-range container handler (+ (* x-amount %1) index), x, (+ (* %1 dy) y), x-amount, dx)) (range y-amount)))

(defn map-player-inventory [container inventory]
  (let [x 6
        dx 18
        dy -18
        x-amount 9
        y-amount 4]
    (add-range container inventory 0 x 163 x-amount dx)
    (add-box container inventory x-amount x 159 x-amount y-amount dx dy)))


(defmacro defblockcontainer [name & args]
  (let [blockdata (apply hash-map args)
        class-name (symbol name)
        prefix (str name "-")
        name-ns (get blockdata :ns *ns*)
        fullname (get-fullname name-ns class-name)
        this-sym (with-meta 'this {:tag fullname})]
    `(do
       (gen-class
         :name ~fullname
         :prefix ~(symbol prefix)
         :extends ~Container
         :init ~'initialize
         :constructors {[~Integer/TYPE PlayerInventory PacketBuffer] [~Integer/TYPE PlayerInventory PacketBuffer]}
         :post-init ~'post-initialize
         :state ~'data)
       (def ~class-name ~fullname)
       ;(def ~class-name (eval '~fullname))
       (import ~fullname)
       (with-prefix ~prefix
         (defn ~'initialize
           ([~'world-id ~'player-inventory ~'packet-buffer]
            [[~'world-id ~'player-inventory ~'packet-buffer]
             (atom {:player-inventory ~'player-inventory
                    :tileentity (.readBlockPos ~'packet-buffer)})]))
         (defn ~'post-initialize
           ([~this-sym]
            nil)
           ([~this-sym ~'world-id ~'player-inventory ~'packet-buffer]
            ;(log/info "^^^^^^^^^^^^^^^^^^" ~x ~'args)
            (map-player-inventory ~this-sym (InvWrapper. ~'player-inventory) )
            ;(.setRegistryName ~'obj ~registry-name)
           ))
         )
       )))

(defn create-container-type [container ^ResourceLocation registry-name]
  (let [factory (proxy [IContainerFactory] []
                  (create [window-id inv data]
                    (construct container window-id inv data)))
        container-type (IForgeContainerType/create factory)
        container-type (.setRegistryName container-type registry-name)]
    container-type))

(defmacro defcontainertype [name container registry-name]
  `(def ~name (create-container-type ~container ~registry-name)))
;IForgeContainerType.create(RPGInventoryContainer::new).setRegistryName("rpg_inventory")

