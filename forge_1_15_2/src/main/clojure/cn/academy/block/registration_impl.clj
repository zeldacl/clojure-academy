(ns cn.academy.block.registration-impl
  (:require [cn.academy.block.registration :as reg]
            [cn.academy.block.material-impl :as material])
  (:import [net.minecraft.block Block Block$Properties]
           [net.minecraft.util ResourceLocation]
           [net.minecraftforge.registries ForgeRegistries]))

(deftype BlockProperties115 []
  reg/IBlockProperties
  (apply-properties! [_ target properties]
    (let [{:keys [hardness resistance light-level]} properties]
      (.. target 
          (hardnessAndResistance hardness resistance)
          (lightValue light-level)))))

(deftype BlockFactory115 []
  reg/IBlockFactory
  (create-block [_ properties event-handlers]
    (let [material-mapper (material/create-material-mapper)
          block-props (Block$Properties/create (.get-material material-mapper (:material properties)))]
      (proxy [Block] [block-props]
        (onBlockActivated [state world pos player hand direction hit]
          ((:on-block-activated event-handlers) world pos state player))
        (onBlockPlacedBy [world pos state placer stack]
          ((:on-block-placed event-handlers) world pos state placer stack))))))

(deftype BlockRegistration115 []
  reg/IBlockRegistration
  (register-block! [_ block-def mod-id block-id]
    (let [block (reg/create-block (BlockFactory115.)
                                 (:properties block-def)
                                 (:event-handlers block-def))]
      (.register ForgeRegistries/BLOCKS
                (doto block
                  (.setRegistryName (str mod-id ":" block-id)))))))

(defn create-registration []
  (BlockRegistration115.))