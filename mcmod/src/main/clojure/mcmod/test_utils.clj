(ns mcmod.test-utils
  (:require [mcmod.protocols :refer :all]
            [mcmod.capabilities :as cap])
  (:import [net.minecraft.world World]
           [net.minecraft.util.math BlockPos]))

(defprotocol IMockWorld
  (get-tile-entity [this pos])
  (set-tile-entity [this pos te])
  (remove-tile-entity [this pos]))

(defrecord MockWorld []
  IMockWorld
  (get-tile-entity [this pos]
    (get-in @(:tile-entities this) [(:x pos) (:y pos) (:z pos)]))
  
  (set-tile-entity [this pos te]
    (swap! (:tile-entities this) assoc-in [(:x pos) (:y pos) (:z pos)] te))
  
  (remove-tile-entity [this pos]
    (swap! (:tile-entities this) update-in [(:x pos) (:y pos)] dissoc (:z pos))))

(defn create-mock-world []
  (->MockWorld {:tile-entities (atom {})}))

(defn create-mock-pos [x y z]
  {:x x :y y :z z})

(defmacro with-test-world [[world-sym] & body]
  `(let [~world-sym (create-mock-world)]
     ~@body))