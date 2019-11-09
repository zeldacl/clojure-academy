(ns cn.li.mcmod.registry)


(defonce ^:dynamic *registry-blocks* (atom []))

(defn registry-block [block]
  (swap! *registry-blocks* conj block))

(defn on-blocks-registry [event]
  ((map #() seq)))