(ns cn.li.academy-api.items
  (:use [cn.li.forge-api.items :only [defitem]])
  (:use [cn.li.academy.tabs :only [tab-academy-craft]]))

(defmacro defACItem [item-name & options]
  (let [item-data (apply hash-map options)
        item-data (assoc-in item-data [:attrs :unlocalized-name] (get-in item-data [:attrs :unlocalized-name] (str "ac_" item-name)))
        item-data (assoc-in item-data [:attrs :texture-name] (get-in item-data [:attrs :texture-name] (str "academy:" item-name)))
        item-data (assoc-in item-data [:attrs :create-tab] (get-in item-data [:attrs :create-tab] ""))
        item-data (reverse (reduce into () item-data))]
    `(do
       (defitem ~item-name ~@item-data))))
