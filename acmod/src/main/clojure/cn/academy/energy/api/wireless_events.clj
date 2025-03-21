(ns cn.academy.energy.api.wireless-events
  (:require [cn.academy.energy.api.wireless :as wireless]
            [mcmod.event :as event]))

(defrecord CreateNetworkEvent [matrix ssid password cancelled]
  event/ICancellable
  (cancel! [this] (reset! cancelled true))
  (is-cancelled? [this] @cancelled))

(defrecord DestroyNetworkEvent [network cancelled]
  event/ICancellable
  (cancel! [this] (reset! cancelled true))
  (is-cancelled? [this] @cancelled))

(defrecord LinkNodeEvent [node network password cancelled]
  event/ICancellable
  (cancel! [this] (reset! cancelled true))
  (is-cancelled? [this] @cancelled))

(defrecord UnlinkNodeEvent [node network cancelled]
  event/ICancellable
  (cancel! [this] (reset! cancelled true))
  (is-cancelled? [this] @cancelled))

(defrecord LinkUserEvent [user node cancelled]
  event/ICancellable
  (cancel! [this] (reset! cancelled true))
  (is-cancelled? [this] @cancelled))

(defrecord UnlinkUserEvent [user cancelled]
  event/ICancellable
  (cancel! [this] (reset! cancelled true))
  (is-cancelled? [this] @cancelled))

(defrecord ChangePasswordEvent [network old-pass new-pass cancelled]
  event/ICancellable
  (cancel! [this] (reset! cancelled true))
  (is-cancelled? [this] @cancelled))

(defn cancel! [event]
  (event/cancel! event))

(defn is-cancelled? [event]
  (event/is-cancelled? event))

(defn get-matrix [event]
  (:matrix event))

(defn get-node [event]
  (:node event))

(defn get-user [event]
  (:user event))

(defn get-tile [event]
  (or (:node event) (:matrix event)))

(defn get-network [event]
  (:network event))

(defn get-password [event]
  (:password event))

(defn get-old-password [event]
  (:old-pass event))

(defn get-new-password [event]
  (:new-pass event))

(defn get-ssid [event]
  (:ssid event))

(defn create-network! [matrix ssid password]
  (let [event (->CreateNetworkEvent matrix ssid password (atom false))]
    (wireless/post-event! event)
    (not (is-cancelled? event))))

(defn destroy-network! [network]
  (let [event (->DestroyNetworkEvent network (atom false))]
    (wireless/post-event! event)
    (not (is-cancelled? event))))

(defn link-node! [node network password]
  (let [event (->LinkNodeEvent node network password (atom false))]
    (wireless/post-event! event)
    (not (is-cancelled? event))))

(defn unlink-node! [node network]
  (let [event (->UnlinkNodeEvent node network (atom false))]
    (wireless/post-event! event)
    (not (is-cancelled? event))))

(defn link-user! [user node]
  (let [event (->LinkUserEvent user node (atom false))]
    (wireless/post-event! event)
    (not (is-cancelled? event))))

(defn unlink-user! [user]
  (let [event (->UnlinkUserEvent user (atom false))]
    (wireless/post-event! event)
    (not (is-cancelled? event))))

(defn change-password! [network old-pass new-pass]
  (let [event (->ChangePasswordEvent network old-pass new-pass (atom false))]
    (wireless/post-event! event)
    (not (is-cancelled? event))))