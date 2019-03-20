(ns cn.li.academy.core
  (:require
    [cn.li.mcmod.core :refer [defmod]]))


(defmod clj-academy "0.1.0"
  :proxy {}
  :common {
           :pre-init (fn [this event])}
  :client {}
  :server {}

  :client-pre-init (fn [this event]))