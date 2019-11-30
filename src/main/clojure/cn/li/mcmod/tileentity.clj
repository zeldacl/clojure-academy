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
        this-sym (with-meta 'this {:tag fullname})]
    `(do
       (gen-class
         :name ~fullname
         :prefix ~(symbol prefix)
         :extends ~TileEntity
         :init ~'initialize
         :constructors {[] []}
         :post-init ~'post-initialize
         :state ~'state
         :exposes-methods {
                           ~'write ~'superWrite,
                           ~'read  ~'superRead
                           })
       (with-prefix ~prefix
         (defn ~'read [~'this ~'compound]
           (~'.superRead ~this-sym ~'compound)
           (read-tag-data! (~'.-state ~this-sym) ~'compound)
           ;(~on-load ~this-sym)
           )
         (defn ~'write [~'this ~'compound]
           (~'.superWrite ~this-sym ~'compound)
           ;(~on-save ~this-sym)
           (write-tag-data! (~'.-state ~this-sym) ~'compound))))))
