(ns cn.li.forge-api.items
  (:use [cn.li.forge-api.core :only [defclass]])
  (:import (net.minecraft.item Item)))

(defmacro defitem [item-name & options]
  (let [item-data (apply hash-map options)
        item-data (assoc item-data :extends Item)]
    `(do
       (defclass ~item-name ~item-data))))
