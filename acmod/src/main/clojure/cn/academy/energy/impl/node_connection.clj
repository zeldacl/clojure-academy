(ns cn.academy.energy.impl.node-connection
  (:require [cn.academy.energy.api.wireless :as wireless]
            [cn.lambdalib2.util.math :as math])
  (:import [net.minecraft.nbt NBTTagCompound]))

(defprotocol INodeConnection
  (get-node [this])
  (get-users [this])
  (add-user! [this user])
  (remove-user! [this user])
  (get-load [this])
  (get-capacity [this])
  (get-bandwidth [this])
  (tick! [this])
  (validate [this])
  (save-to-nbt [this])
  (load-from-nbt! [this tag]))

(defrecord NodeConnection [world-data node users]
  INodeConnection
  (get-node [_] 
    @node)
  
  (get-users [_] 
    @users)
  
  (add-user! [_ user]
    (when (wireless/is-wireless-user? user)
      (swap! users conj user)))
  
  (remove-user! [_ user]
    (swap! users disj user))
  
  (get-load [_]
    (count @users))
  
  (get-capacity [this]
    (when-let [n (get-node this)]
      (.getMaxConnections n)))
  
  (get-bandwidth [this]
    (when-let [n (get-node this)]
      (.getBandwidth n)))
  
  (tick! [this]
    (when-let [node (get-node this)]
      (let [bandwidth (get-bandwidth this)
            transfer-left (atom bandwidth)]
        (doseq [user @users]
          (when (pos? @transfer-left)
            (cond
              ;; Handle generators
              (wireless/is-generator? user)
              (let [gen-amount (.getGenerationRate user)
                    actual-amount (min gen-amount @transfer-left)]
                (.generateEnergy user actual-amount)
                (swap! transfer-left - actual-amount))
              
              ;; Handle receivers
              (wireless/is-receiver? user)
              (let [needed (.getEnergyNeeded user)
                    actual-amount (min needed @transfer-left)]
                (.receiveEnergy user actual-amount false)
                (swap! transfer-left - actual-amount))))))))
  
  (validate [this]
    (and (get-node this)
         (every? wireless/is-wireless-user? (get-users this))))
  
  (save-to-nbt [this]
    (let [tag (NBTTagCompound.)
          node-tag (NBTTagCompound.)]
      (wireless/save-node-to-nbt! (get-node this) node-tag)
      (.setTag tag "node" node-tag)
      ;; Save users list
      (let [users-tag (NBTTagCompound.)]
        (doseq [[idx user] (map-indexed vector (get-users this))]
          (let [user-tag (NBTTagCompound.)]
            (wireless/save-user-to-nbt! user user-tag)
            (.setTag users-tag (str idx) user-tag)))
        (.setTag tag "users" users-tag))
      tag))
  
  (load-from-nbt! [this tag]
    (reset! node (wireless/load-node-from-nbt! (.getTag tag "node")))
    (let [users-tag (.getTag tag "users")]
      (reset! users
        (into #{}
          (for [idx (range (.getSize users-tag))]
            (wireless/load-user-from-nbt! (.getTag users-tag (str idx)))))))))

(defn create [world-data node]
  (->NodeConnection 
    world-data
    (atom node)
    (atom #{})))