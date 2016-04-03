(ns cn.li.academy.energy.blocks
  (:use [cn.li.academy-api.blocks :only [defACBlockContainer]])
  (:use [cn.li.academy.energy.tileentitis :only [tile-entity-node get-max-energy]])
  (:use [cn.li.academy.energy.gui :only [node-gui-handler]])
  (:use [cn.li.academy.tabs :only [tab-academy-craft]])
  (:use [cn.li.forge-api.utils :only [setfield getfield get-tile-entity]])
  (:use [cn.li.academy.config :only [node-config]])
  (:import (net.minecraft.client.renderer.texture IIconRegister)
           (net.minecraft.world IBlockAccess World)))




(defmacro defblocknode [block-name type]
  (let [ _ (print  "dddd" block-name type)
        prefix (str block-name "-")
        blockdata {
                   :attrs       {
                                 :block-name    "block-node"
                                 :hardness      (float 2.5)
                                 :create-tab    tab-academy-craft
                                 :harvest-level ["pickaxe", 1]
                                 }
                   :fields      {
                                 :icon-top-disabled nil
                                 :icon-top-enabled  nil
                                 :side-icons        []
                                 }
                   :tile-entity tile-entity-node
                   :gui-handler node-gui-handler
                   }
        _ (print blockdata)
        blockdata (reverse (reduce into () blockdata))
        _ (print blockdata)]
    `(do
       (defACBlockContainer ~block-name ~@blockdata)
       (defn ~(symbol (str prefix "registerBlockIcons")) [~'this ^IIconRegister ~'ir]
         (setfield ~'this :icon-top-disabled (.registerIcon ~'ir "academy:node_top_0"))
         (setfield ~'this :icon-top-enabled (.registerIcon ~'ir "academy:node_top_1"))
         (setfield ~'this :side-icons (mapv #(.registerIcon ~'ir (str "academy:node_" + (name ~type) + "_side_" + %1)) (range 5)))
         )
       (defn ~(symbol (str prefix "getIcon"))
         ([~'this ~'side ~'meta]
           (if (or (= ~'side 0) (= ~'side 1))
             (getfield ~'this :icon-top-enabled)
             (get (getfield ~'this :side-icons) 1)))
         ([~'this ^IBlockAccess ~'world ~'x ~'y ~'z ~'side]
           (let [te# (.getTileEntity ~'world ~'x ~'y ~'z)
                 [enable# pct#] (if (instance? ~tile-entity-node te#)
                                  (let [node# te#
                                        enabled1# (getfield node# :enabled)
                                        pct1# (min 4 (Math/round (/ (* 4 (getfield node# :energy)) (get-max-energy node#))))]
                                    [enabled1# pct1#])
                                  [false 0])]
             (if (or (= ~'side 0) (= ~'side 1))
               (if enable#
                 (getfield ~'this :icon-top-enabled)
                 (getfield ~'this :icon-top-disabled))
               (get (getfield ~'this :side-icons) pct#))))
         )
       (defn ~(symbol (str prefix "onBlockPlaced")) [~'this ^World ~'world ^int ~'x ^int ~'y ^int ~'z ^int ~'side
                                                     ^float ~'tx ^float ~'ty ^float ~'tz ^int ~'meta]
         ~(:id ((keyword type) node-config)))
       (def ~(symbol (str prefix "getRenderBlockPass"))
         (constantly -1))
       (def ~(symbol (str prefix "isOpaqueCube"))
         (constantly false))
       )))

(print "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb$$$$$$$$$$$^^^^^^^^^^^^^^^^^!!!!!!!!!!!!!!!!!!!!!!!")
(defblocknode block-node-basic :basic)