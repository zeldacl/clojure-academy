(ns cn.li.academy.energy.ui.slots
  (:require [cn.li.mcmod.ui :refer [defcontainerslot]])
  (:import (net.minecraft.item ItemStack)))

(defcontainerslot slot-ifitem
  :overrides {
              :isItemValid (fn [^ItemStack stack]
                             ;todo
                             false)
              })
