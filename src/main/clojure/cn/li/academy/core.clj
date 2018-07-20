(ns cn.li.academy.core
  (:require
    [cn.li.mcmod.core :refer [defmod]]))


(defmod clj-academy "0.1.0"
  :pre-init (fn [this event])
  :client-pre-init (fn [this event]))