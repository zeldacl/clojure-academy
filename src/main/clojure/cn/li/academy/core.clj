(ns cn.li.academy.core
  (:require
    [cn.li.mcmod.core :refer [defmod]]
    [cn.li.academy.proxy :as proxy]))

(defmod clj-academy
        :modid "clj-academy"
        :version "0.1.0"
        :proxy {:client proxy/client-proxy
                :server ""})