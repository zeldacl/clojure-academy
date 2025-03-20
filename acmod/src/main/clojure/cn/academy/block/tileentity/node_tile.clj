(ns cn.academy.block.tileentity.node-tile
  (:require [mcmod.protocols :refer :all]
            [cn.academy.block.block-node :as node]
            [clojure.tools.logging :as log]))

(defn- calculate-connection-points [pos range]
  (let [[x y z] pos
        radius range]
    (for [dx (range (- radius) (inc radius))
          dy (range (- radius) (inc radius))
          dz (range (- radius) (inc radius))
          :let [dist (Math/sqrt (+ (* dx dx) (* dy dy) (* dz dz)))]
          :when (<= dist radius)]
      [(+ x dx) (+ y dy) (+ z dz)])))

(defrecord WirelessNodeTile [node-type energy connections]
  ITileEntity
  (tick [this]
    (when (and (not (is-remote? (get-world this)))
               (> (count @connections) 0))
      (distribute-energy this)))
  
  (save [this]
    {:energy @energy
     :connections (vec @connections)
     :node-type node-type})
     
  (load [this data]
    (reset! energy (:energy data 0))
    (reset! connections (:connections data []))
    (assoc this :node-type (:node-type data :basic)))

  IEnergyStorage  
  (receive-energy [this amount simulate]
    (let [max-energy (get-in node/node-types [node-type :max-energy])
          current @energy
          space-left (- max-energy current)
          actual-receive (min amount space-left)]
      (when-not simulate
        (swap! energy + actual-receive))
      actual-receive))
      
  (extract-energy [this amount simulate]
    (let [current @energy
          actual-extract (min amount current)]
      (when-not simulate  
        (swap! energy - actual-extract))
      actual-extract))
      
  (get-energy-stored [this]
    @energy)
    
  (get-max-energy-stored [this]
    (get-in node/node-types [node-type :max-energy]))

  IWirelessNode
  (get-range [this]
    (get-in node/node-types [node-type :range]))
    
  (get-max-connections [this]
    (get-in node/node-types [node-type :max-connections]))
    
  (get-energy [this]
    @energy)
    
  (can-connect? [this other]
    (and (instance? WirelessNodeTile other)
         (< (count @connections) (get-max-connections this))))
         
  (connect [this other]
    (when (can-connect? this other)
      (swap! connections conj (get-position other))
      true))
      
  (disconnect [this other]
    (swap! connections disj (get-position other))
    true))

(defn create-node-tile [node-type]
  (->WirelessNodeTile node-type (atom 0) (atom #{})))

(defn- distribute-energy [this]
  (let [connected-nodes (->> @(:connections this)
                           (map #(get-tile-entity (get-world this) %))
                           (filter identity))
        total-nodes (inc (count connected-nodes))
        energy-per-node (int (/ @(:energy this) total-nodes))]
    (doseq [node connected-nodes]
      (let [transferred (extract-energy this energy-per-node true)]
        (when (> transferred 0)
          (let [accepted (receive-energy node transferred true)]
            (when (> accepted 0)
              (extract-energy this accepted false)
              (receive-energy node accepted false))))))))