(ns cn.li.mcmod.tileentity
  (:require [cn.li.mcmod.utils :refer [get-fullname with-prefix]]
            [cn.li.mcmod.nbt :refer [read-tag-data! write-tag-data!]])
  (:import (net.minecraft.tileentity TileEntity)))

(defmacro deftilerntity [name & args]
  (let [classdata (apply hash-map args)
        name-ns (get classdata :ns *ns*)
        prefix (str name "-")
        ;classdata (assoc-in classdata [:expose 'readFromNBT] 'superReadFromNBT)
        ;classdata (assoc-in classdata [:expose 'writeToNBT] 'superWriteToNBT)
        fullname (get-fullname name-ns name)
        this-sym (with-meta 'this {:tag fullname})
        post-init (:post-init classdata)
        fields (:fields classdata)]
    `(do
       (gen-class
         :name ~fullname
         :prefix ~(symbol prefix)
         :extends ~TileEntity
         :init ~'initialize
         :constructors {[] []}
         :post-init ~'post-initialize
         :state ~'data
         :exposes-methods {
                           ~'save ~'superSave,
                           ~'load  ~'superLoad
                           })
       (def ~name ~fullname)
       (import ~fullname)
       (with-prefix ~prefix
         (defn ~'initialize
           ([~'& ~'args]
            [[] (atom ~fields)]))
         (defn ~'post-initialize [~'this ~'& ~'args]
           (when ~post-init
             (~post-init ~'this ~'args)))
         (defn ~'load [~'this ~'compound]
           (~'.superLoad ~this-sym ~'compound)
           (read-tag-data! (~'.-data ~this-sym) ~'compound)
           ;(~on-load ~this-sym)
           )
         (defn ~'save [~'this ~'compound]
           (~'.superSave ~this-sym ~'compound)
           ;(~on-save ~this-sym)
           (write-tag-data! (~'.-data ~this-sym) ~'compound))))))
