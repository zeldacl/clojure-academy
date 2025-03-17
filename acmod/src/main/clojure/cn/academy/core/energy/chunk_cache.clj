(ns cn.academy.core.energy.chunk-cache
  (:import [java.util WeakHashMap]))

(def ^:private chunk-cache (WeakHashMap.))

(defprotocol INodeCache
  (add-node! [this pos node])
  (remove-node! [this pos])
  (get-nodes [this])
  (clear! [this]))

(defrecord ChunkNodeCache []
  INodeCache
  (add-node! [_ pos node]
    (.put chunk-cache pos node))
  
  (remove-node! [_ pos]
    (.remove chunk-cache pos))
  
  (get-nodes [_]
    (vec (.values chunk-cache)))
  
  (clear! [_]
    (.clear chunk-cache)))

(defn get-chunk-key [world chunk-x chunk-z]
  (str (.dimension world) ":" chunk-x ":" chunk-z))

(defn get-or-create-cache [world chunk-x chunk-z]
  (let [key (get-chunk-key world chunk-x chunk-z)]
    (or (.get chunk-cache key)
        (let [cache (->ChunkNodeCache)]
          (.put chunk-cache key cache)
          cache))))

(defn update-chunk-nodes! [world chunk-x chunk-z nodes]
  (let [cache (get-or-create-cache world chunk-x chunk-z)]
    (clear! cache)
    (doseq [{:keys [pos node]} nodes]
      (add-node! cache pos node))))