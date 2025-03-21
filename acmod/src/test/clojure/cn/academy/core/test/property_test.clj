(ns cn.academy.core.test.property-test
  (:require [clojure.test :refer :all]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]
            [cn.academy.core.util.serialization :as serial]
            [cn.academy.core.energy.chunk-cache :as cache]
            [cn.academy.api.block :as block-api]))

;; Generate block position data structure instead of direct BlockPos
(def gen-pos
  (gen/fmap
    (fn [[x y z]]
      {:x x :y y :z z}) ; Use map instead of BlockPos
    (gen/tuple (gen/choose -30000000 30000000)
               (gen/choose 0 255)
               (gen/choose -30000000 30000000))))

(def gen-energy-data
  (gen/hash-map
    :stored (gen/double* {:min 0.0 :max 1000000.0})
    :capacity (gen/double* {:min 1000.0 :max 10000000.0})))

(defspec test-energy-serialization-properties 100
  (prop/for-all [data gen-energy-data]
    (let [storage (serial/deserialize-energy-data data 1000)
          serialized (serial/serialize-energy-data storage)]
      (and (= (:stored data) (:stored serialized))
           (= (:capacity data) (:capacity serialized))))))

(defspec test-chunk-cache-properties 100
  (prop/for-all [positions (gen/vector gen-pos 1 10)
                 nodes (gen/vector gen/string-alphanumeric 1 10)]
    (let [cache (cache/->ChunkNodeCache)]
      (doseq [[pos node] (map vector positions nodes)]
        (cache/add-node! cache pos node))
      (and (= (count positions) (count (cache/get-nodes cache)))
           (every? #(some #{%} nodes) (cache/get-nodes cache))))))

(defspec test-pos-serialization 100
  (prop/for-all [pos gen-pos]
    (let [nbt (block-api/create-nbt-compound)
          _ (serial/write-pos-to-nbt pos nbt)
          read-pos (serial/read-pos-from-nbt nbt)]
      (and (= (:x pos) (:x read-pos))
           (= (:y pos) (:y read-pos))
           (= (:z pos) (:z read-pos))))))