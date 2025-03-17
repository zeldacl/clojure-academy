(ns cn.academy.client-handler
  (:require [net.minecraftforge.fml.common.event :refer [FMLInitializationEvent]]
            [net.minecraftforge.common :refer [MinecraftForge]]
            [net.minecraftforge.common.config :refer [Configuration]]
            [org.lwjgl.input :refer [Keyboard]]))

(def key-switch-preset "switch_preset")
(def key-edit-preset "edit_preset")
(def key-activate-ability "ability_activation")

(def key-ids-init [Keyboard/KEY_R Keyboard/KEY_F])
(def key-ids (atom (vec key-ids-init)))

(defn update-ability-keys []
  (let [cfg (Configuration.)]
    (doseq [i (range (count @key-ids))]
      (swap! key-ids assoc i (.getInt cfg (str "ability_" i) "keys" (nth key-ids-init i) -1000 1000 (str "Ability control key #" i))))
    (MinecraftForge/EVENT_BUS/post (Object.))))

(defn init [^FMLInitializationEvent ev]
  (update-ability-keys))

(MinecraftForge/EVENT_BUS/register (Object. {:init init}))