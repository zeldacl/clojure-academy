(ns cn.li.academy.tab
  (:require
    [forge-clj.items :refer [deftab]]
    [forge-clj.util :refer [get-item]]))

(defn tab-clojure-academy-item []
  (get-item "academy:logo"))

(deftab tab-clojure-academy
        :override {:get-tab-icon-item tab-clojure-academy-item})
