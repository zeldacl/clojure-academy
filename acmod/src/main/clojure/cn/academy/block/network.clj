(ns cn.academy.block.network
  (:require [cn.academy.tech-system.energy-system.network.optimization :as optimization]
            [cn.academy.tech-system.energy-system.network.handler :as network-handler]
            [cn.academy.tech-system.energy-system.network.sync :as sync]
            [clojure.tools.logging :as log]))

(defn init-network! []
  ;; Register message handlers with energy system
  (network-handler/register-handler! :block-destroyed 
    (fn [block]
      (when-let [pos (mcmod.block/get-pos block)]
        (mcmod.block/remove-block! pos))))
  
  (network-handler/register-handler! :update-energy
    (fn [block amount]
      (when-let [energy-storage (mcmod.block/get-energy-storage block)]
        (.set-energy-stored! energy-storage amount))))
  
  (network-handler/register-handler! :update-progress
    (fn [block progress]
      (when-let [machine (mcmod.block/get-machine-state block)]
        (.set-progress! machine progress))))
  
  (network-handler/register-handler! :multiblock-formed
    (fn [controller members]
      (doseq [member members]
        (when-let [mb-part (mcmod.block/get-multiblock-part member)]
          (.set-controller! mb-part controller)))))
  
  (network-handler/register-handler! :multiblock-broken
    (fn [member]
      (when-let [mb-part (mcmod.block/get-multiblock-part member)]
        (.set-controller! mb-part nil))))
  
  (log/info "Block network handlers registered with energy system"))