(ns cn.academy.energy.api.wireless-events
  (:require [cn.academy.energy.api.wireless :as wireless])
  (:import [net.minecraftforge.eventbus.api Event]))

(defrecord CreateNetworkEvent [matrix ssid password]
  Event
  (isCancelable [_] true))

(defrecord DestroyNetworkEvent [network]
  Event
  (isCancelable [_] true))

(defrecord LinkNodeEvent [node network password]
  Event
  (isCancelable [_] true))

(defrecord UnlinkNodeEvent [node network]
  Event
  (isCancelable [_] true))

(defrecord LinkUserEvent [user node]
  Event
  (isCancelable [_] true))

(defrecord UnlinkUserEvent [user]
  Event
  (isCancelable [_] true))

(defrecord ChangePasswordEvent [network old-pass new-pass]
  Event
  (isCancelable [_] true))

(defn create-network! [matrix ssid password]
  (let [event (->CreateNetworkEvent matrix ssid password)]
    (wireless/post-event! event)
    (not (.isCanceled event))))

(defn destroy-network! [network]
  (let [event (->DestroyNetworkEvent network)]
    (wireless/post-event! event)
    (not (.isCanceled event))))

(defn link-node! [node network password]
  (let [event (->LinkNodeEvent node network password)]
    (wireless/post-event! event)
    (not (.isCanceled event))))

(defn unlink-node! [node network]
  (let [event (->UnlinkNodeEvent node network)]
    (wireless/post-event! event)
    (not (.isCanceled event))))

(defn link-user! [user node]
  (let [event (->LinkUserEvent user node)]
    (wireless/post-event! event)
    (not (.isCanceled event))))

(defn unlink-user! [user]
  (let [event (->UnlinkUserEvent user)]
    (wireless/post-event! event)
    (not (.isCanceled event))))

(defn change-password! [network old-pass new-pass]
  (let [event (->ChangePasswordEvent network old-pass new-pass)]
    (wireless/post-event! event)
    (not (.isCanceled event))))