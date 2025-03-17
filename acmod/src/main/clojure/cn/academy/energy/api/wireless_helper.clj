(ns cn.academy.energy.api.wireless-helper
  (:require [cn.academy.api.energy :as energy]))

(defprotocol IWirelessNode
  (get-node-name [this])
  (get-max-energy [this])
  (get-current-energy [this])
  (receive-energy [this amount])
  (get-range [this]))

(defprotocol IWirelessGenerator
  (has-node? [this])
  (get-linked-node [this])
  (link-to-node! [this node])
  (unlink! [this]))

(defn get-nodes-in-range [world pos]
  (energy/get-nearby-nodes world pos))

(defn generator-linked? [generator]
  (has-node? generator))

(defn link-generator! [generator node]
  (link-to-node! generator node))

(defn unlink-generator! [generator]
  (unlink! generator))