(ns cn.li.academy.datagen
  (:require [cn.li.mcmod.utils :refer [listen-forge-bus]])
  (:import (net.minecraftforge.fml.event.lifecycle GatherDataEvent)))

(listen-forge-bus net.minecraftforge.fml.event.lifecycle.GatherDataEvent (fn [^net.minecraftforge.fml.event.lifecycle.GatherDataEvent event] nil))

;(macroexpand-1 (utils/listen-forge-bus GatherDataEvent (fn [^GatherDataEvent event] nil)))

;(clojure.pprint/pprint (macroexpand-1 '(listen-forge-bus GatherDataEvent (fn [^GatherDataEvent event] nil))))
