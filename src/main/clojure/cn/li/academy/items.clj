(ns cn.li.academy.items
  (:require [forge-clj.items :refer [defitem]]
            [cn.li.academy.tab :refer [tab-clojure-academy]]))

(defitem logo
         :unlocalized-name "logo"
         :creative-tab tab-clojure-academy
         :texture-name "clojureacademy:logo")
