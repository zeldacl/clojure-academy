(ns cn.li.forge-api.gui
  (:use [cn.li.forge-api.core :only [defclass]])
  (:import (cpw.mods.fml.common.network IGuiHandler)
           (net.minecraft.entity.player EntityPlayer)
           (net.minecraft.world World)
           (net.minecraft.inventory Container)))

(defmacro defguihandler
  [handler-name get-server-container get-client-container]
  `(def ~handler-name (reify IGuiHandler
                        (~'getServerGuiElement [~'this ~'id ~'player ~'world ~'x ~'y ~'z]
                          (~get-server-container ~'id ~'player ~'world ~'x ~'y ~'z))
                        (~'getClientGuiElement [~'this ~'id ~'player ~'world ~'x ~'y ~'z]
                          (~get-client-container ~'id ~'player ~'world ~'x ~'y ~'z)))))

(defn open-gui-container [^EntityPlayer player mod-instance id ^World world x y z]
  (when (not (.isRemote world))
    (.openGui player mod-instance id world x y z)))


(defmacro defcontainer [container-name & options]
  (let [                                                    ;prefix (str container-name "-")
        container-data (apply hash-map options)
        container-data (assoc container-data :extends Container)
        ;on-init (get container-data :on-init `(constantly nil))
        container-data (assoc container-data :init-fn '([player-inventory tile-entity]
                                                         [[] {:player-inventory player-inventory
                                                              :tile-entity tile-entity}]))]
    `(do
       (defclass ~container-name ~container-data))))