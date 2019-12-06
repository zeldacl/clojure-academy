(ns cn.li.mcmod.item
  (:require [cn.li.mcmod.utils :refer [get-fullname with-prefix]]
            [cn.li.mcmod.registry :refer [set-registry-name registry-item *item-group*]])
  (:import (net.minecraft.item Item Item$Properties ItemGroup ItemStack)))

(defmacro defitem [name & args]
  (let [blockdata (apply hash-map args)
        class-name (symbol name)
        prefix (str name "-")
        name-ns (get blockdata :ns *ns*)
        fullname (get-fullname name-ns class-name)
        registry-name (:registry-name blockdata)
        this-sym (with-meta 'this {:tag fullname})]
    `(do
       (gen-class
         :name ~fullname
         :prefix ~(symbol prefix)
         :extends ~Item
         :init ~'initialize
         :constructors {[] [Item$Properties]}
         :post-init ~'post-initialize
         :state ~'data
         :exposes-methods {
                           ~'write ~'superWrite,
                           ~'read  ~'superRead
                           })
       (def ~name ~fullname)
       (import ~fullname)
       (with-prefix ~prefix
         (defn ~'initialize
           ([~'& ~'args]
            [[(Item$Properties.)] []]))
         (defn ~'post-initialize [~'this ~'& ~'args]
           ~(when registry-name
              ;`(.setRegistryName ~x ~registry-name)
              `(set-registry-name ~'this ~registry-name)
              ))))))



(defn create-item-group [label icon]
  (let [item (proxy [Item] [(Item$Properties.)])
        item (set-registry-name item icon)]
    (registry-item item)
    (proxy [ItemGroup] [label]
      (createIcon []
        (ItemStack. item)))))

(defn init-item-group [label icon]
  (alter-var-root #'*item-group* (constantly (create-item-group label icon))))
