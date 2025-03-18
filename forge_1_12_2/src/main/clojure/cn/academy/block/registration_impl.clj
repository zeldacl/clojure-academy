(ns cn.academy.block.registration-impl
  (:require [cn.academy.block.registration :as reg]
            [cn.academy.block.material-impl :as material])
  (:import [net.minecraftforge.fml.common.registry GameRegistry]
           [net.minecraft.block Block]
           [net.minecraft.util ResourceLocation]))

(deftype BlockProperties112 []
  reg/IBlockProperties
  (apply-properties! [_ target properties]
    (let [{:keys [hardness resistance light-level]} properties]
      (doto target
        (.setHardness hardness)
        (.setResistance resistance)
        (.setLightLevel light-level)))))

(deftype BlockFactory112 []
  reg/IBlockFactory
  (create-block [_ properties event-handlers]
    (let [material-mapper (material/create-material-mapper)]
      (doto (proxy [Block] [(.get-material material-mapper (:material properties))]
              (onBlockActivated [world pos state player hand facing hitX hitY hitZ]
                ((:on-block-activated event-handlers) world pos state player))
              (onBlockPlacedBy [world pos state placer stack]
                ((:on-block-placed event-handlers) world pos state placer stack)))))))

(deftype BlockRegistration112 []
  reg/IBlockRegistration
  (register-block! [_ block-def mod-id block-id]
    (let [block (reg/create-block (BlockFactory112.)
                                 (:properties block-def)
                                 (:event-handlers block-def))]
      (GameRegistry/register 
        (doto block
          (.setRegistryName (str mod-id ":" block-id)))))))

(defn create-registration []
  (BlockRegistration112.))