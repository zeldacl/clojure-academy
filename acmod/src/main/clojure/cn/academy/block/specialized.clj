(ns cn.academy.block.specialized
  (:require [cn.academy.block.component :as component]
            [cn.academy.block.registry :as registry]))

;; Processor block implementation
(defn create-processor [& {:keys [processing-time]
                          :or {processing-time 100}}]
  (let [inventory (component/create-inventory 2)
        state (atom {:progress 0
                    :processing-time processing-time})]
    {:inventory inventory
     :state state
     :on-tick (fn []
                (when-let [input (component/get-slot inventory 0)]
                  (swap! state update :progress inc)
                  (when (>= (:progress @state) processing-time)
                    (component/set-slot! inventory 0 nil)
                    (component/set-slot! inventory 1 {:id "processed_item" :count 1})
                    (swap! state assoc :progress 0))))}))

;; Matrix block implementation 
(defrecord MatrixBlock [energy inventory network]
  component/IBlockComponent
  (update! [_]
    (component/update! energy)
    (component/update! network))
  
  (get-capability [_ type side]
    (case type
      :energy energy
      :inventory inventory
      :network network
      nil))
  
  (serialize [_]
    {:energy (component/serialize energy)
     :inventory (component/serialize inventory)
     :network (component/serialize network)})
  
  (deserialize! [_ data]
    (component/deserialize! energy (:energy data))
    (component/deserialize! inventory (:inventory data))
    (component/deserialize! network (:network data))))

(defn create-matrix []
  (let [energy (component/create-energy 100000)
        inventory (component/create-inventory 4)
        network (component/create-component :network :wireless {})]
    (->MatrixBlock energy inventory network)))

;; Node block variations
(defn- create-node-base [level]
  (let [base-energy (* level 20000)
        base-bandwidth (* level 1000)]
    {:energy (component/create-energy base-energy)
     :network (component/create-component :network :wireless 
                                        {:bandwidth base-bandwidth
                                         :range (* level 16)})}))

(defn create-node [type]
  (case type
    :basic (create-node-base 1)
    :standard (create-node-base 2) 
    :advanced (create-node-base 4)))