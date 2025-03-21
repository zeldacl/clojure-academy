(ns cn.academy.block.machine
  (:require [mcmod.protocols :refer [IMachineState IEnergyStorage IResourceHandler]]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; Machine state tracking
(def machine-state
  (atom {:active-machines {}}))

;; Machine state implementation
(defrecord MachineState [block state-atom]
  IMachineState
  (is-active? [_]
    (get @state-atom :active false))
  
  (set-active! [_ active]
    (swap! state-atom assoc :active active))
  
  (get-progress [_]
    (get @state-atom :progress 0))
  
  (set-progress! [_ progress]
    (swap! state-atom assoc :progress progress))
  
  (get-error-state [_]
    (get @state-atom :error nil))
  
  (set-error-state! [_ error]
    (swap! state-atom assoc :error error))

  IEnergyStorage
  (get-energy-stored [_]
    (get @state-atom :energy 0))
  
  (get-max-energy-stored [_]
    (get-in block [:properties :max-energy]))
  
  (receive-energy [_ amount simulate]
    (let [current (get @state-atom :energy 0)
          max-energy (get-in block [:properties :max-energy])
          accepted (min amount (- max-energy current))]
      (when-not simulate
        (swap! state-atom update :energy + accepted))
      accepted))
  
  (extract-energy [_ amount simulate]
    (let [current (get @state-atom :energy 0)
          extracted (min amount current)]
      (when-not simulate
        (swap! state-atom update :energy - extracted))
      extracted))

  IResourceHandler
  (has-resources? [_ resources]
    (every? (fn [{:keys [type amount]}]
              (>= (get-in @state-atom [:resources type] 0) amount))
            resources))
  
  (has-space? [_ resources]
    (every? (fn [{:keys [type amount]}]
              (let [current (get-in @state-atom [:resources type] 0)
                    limit (get-in block [:properties :resource-limits type])]
                (<= (+ current amount) (or limit Integer/MAX_VALUE))))
            resources))
  
  (add-resources! [_ resources]
    (doseq [{:keys [type amount]} resources]
      (swap! state-atom update-in [:resources type] 
             #(+ (or % 0) amount))))
  
  (consume-resources! [_ resources]
    (doseq [{:keys [type amount]} resources]
      (swap! state-atom update-in [:resources type] 
             #(- (or % 0) amount)))))

;; Machine factory functions
(defn create-machine-state [block]
  (let [state (->MachineState block (atom {:energy 0
                                          :active false
                                          :progress 0
                                          :resources {}}))]
    (swap! machine-state assoc-in [:active-machines (:id block)] state)
    state))

(defn get-machine-state [block]
  (get-in @machine-state [:active-machines (:id block)]))

;; Machine lifecycle management
(defn start-machine! [block]
  (when-let [state (get-machine-state block)]
    (.set-active! state true)
    (.set-error-state! state nil)))

(defn stop-machine! [block]
  (when-let [state (get-machine-state block)]
    (.set-active! state false)))

;; Machine error handling
(defn handle-machine-error! [block error]
  (when-let [state (get-machine-state block)]
    (.set-active! state false)
    (.set-error-state! state error)))

;; Initialize machine system
(defn init-machines! []
  (reset! machine-state {:active-machines {}}))