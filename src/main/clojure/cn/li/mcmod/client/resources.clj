(ns cn.li.mcmod.client.resources
  (:require [cn.li.mcmod.global :refer [*mod-id*]])
  (:import (net.minecraft.util ResourceLocation)))


(defn- res [name]
  (ResourceLocation. *mod-id* name))

(defn load-texture [name]
  (res (str "textures/" name ".png")))
