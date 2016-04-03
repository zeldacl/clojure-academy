(ns cn.li.academy-api.blocks
  (:use [cn.li.forge-api.core :only [defclass]])
  (:use [cn.li.forge-api.blocks :only [defBlockcontainer]])
  (:use [cn.li.forge-api.utils :only [drop-items]])
  ;(:use [cn.li.clojureacademy :only [instance]])
  (:use [cn.li.forge-api.gui :only [open-gui-container]])
  (:import (net.minecraft.world World)
           (net.minecraft.entity.player EntityPlayer)
           (net.minecraft.block Block)
           (net.minecraft.inventory IInventory)))

(defmacro defACBlockContainer [block-name & options]
  (let [prefix (str block-name "-")
        block-data (apply hash-map options)
        gui-handler                                         (:gui-handler block-data)
        block-data (assoc-in block-data [:attrs :block-name] (get-in block-data [:attrs :block-name] (str "ac_" block-name)))
        block-data (assoc-in block-data [:attrs :block-texture-name] (get-in block-data [:attrs :block-texture-name] (str "academy:" block-name)))
        block-data (assoc-in block-data [:attrs :create-tab] (get-in block-data [:attrs :create-tab] ""))
        block-data (assoc-in block-data [:exposes-methods 'breakBlock] 'superBreakBlock)
        block-data (reverse (reduce into () block-data))
        _ (print block-data)]
    `(do
       (defBlockcontainer ~block-name ~@block-data)
       (defn ~(symbol (str prefix "onBlockActivated")) [~'this ^World ~'world ^int ~'x ^int ~'y ^int ~'z ^EntityPlayer ~'player ^int ~'side
                                                        ^float ~'tx ^float ~'ty ^float ~'tz]
         (if (and ;~gui-handler
         (not (.isSneaking ~'player))
               )
           (do
             (when (not (.isRemote ~'world))
               ;(open-gui-container ~'player ~instance 0 ~'world ~'x ~'y ~'z)
               )
             true)
           false))
       (defn ~(symbol (str prefix "breakBlock")) [~'this ^World ~'world ^int ~'x ^int ~'y ^int ~'z ^Block ~'block ^int ~'wtf]
         (when (.-isRemote ~'world)
           (let [te# (.getTileEntity ~'world ~'x ~'y ~'z)]
             (when (instance? IInventory te#)
               (drop-items ~'world ~'x ~'y ~'z te#))))
         (~'.superBreakBlock ~'this ~'world ~'x ~'y ~'z ~'block ~'wtf))
       )))
