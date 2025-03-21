(ns cn.academy.block.upgrade
  (:require [mcmod.protocols :refer [IUpgradeHandler IUpgrade]]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; Upgrade state tracking
(def upgrade-state
  (atom {:registered-upgrades {}
         :active-upgrades {}}))

;; Upgrade implementation
(defrecord MachineUpgrade [type modifiers]
  IUpgrade
  (get-type [_]
    type)
  
  (get-modifier [_ property]
    (get modifiers property 1.0))
  
  (can-apply? [_ machine]
    (let [existing (get-in @upgrade-state 
                          [:active-upgrades (:id machine)])]
      (and (< (count existing) 
              (get-in machine [:properties :max-upgrades] 4))
           (every? #(compatible? % type) 
                   (map :type (vals existing))))))
  
  (on-installed [_ machine]
    (swap! upgrade-state update-in 
           [:active-upgrades (:id machine)]
           assoc type this))
  
  (on-removed [_ machine]
    (swap! upgrade-state update-in
           [:active-upgrades (:id machine)]
           dissoc type)))

;; Upgrade handler implementation
(defrecord UpgradeHandler [machine state-atom]
  IUpgradeHandler
  (get-upgrades [_]
    (vals (get-in @upgrade-state [:active-upgrades (:id machine)] {})))
  
  (get-upgrade [_ type]
    (get-in @upgrade-state [:active-upgrades (:id machine) type]))
  
  (install-upgrade [_ upgrade]
    (when (.can-apply? upgrade machine)
      (.on-installed upgrade machine)
      true))
  
  (remove-upgrade [_ type]
    (when-let [upgrade (.get-upgrade this type)]
      (.on-removed upgrade machine)
      true))
  
  (get-property-modifier [this property]
    (let [upgrades (.get-upgrades this)]
      (reduce * 1.0 (map #(.get-modifier % property) upgrades))))
  
  (apply-modifier [_ base property]
    (* base (get-in @state-atom [:modifiers property] 1.0))))

;; Factory functions
(defn create-upgrade [type modifiers]
  (->MachineUpgrade type modifiers))

(defn create-upgrade-handler [machine]
  (->UpgradeHandler machine (atom {:modifiers {}})))

;; Upgrade registration
(defn register-upgrade! [type modifiers]
  (swap! upgrade-state assoc-in [:registered-upgrades type]
         (create-upgrade type modifiers)))

;; Upgrade compatibility checking
(defn compatible? [type1 type2]
  (not= type1 type2))

;; Upgrade property modifiers
(def upgrade-properties
  {:speed {:max 4.0 :min 0.25}
   :energy-usage {:max 2.0 :min 0.5}
   :range {:max 3.0 :min 1.0}
   :capacity {:max 4.0 :min 1.0}})

;; Standard upgrade definitions
(def standard-upgrades
  {:speed {:speed 1.5
           :energy-usage 1.2}
   :efficiency {:speed 0.8
                :energy-usage 0.7}
   :capacity {:capacity 2.0}
   :range {:range 1.5
           :energy-usage 1.1}})

;; Initialize upgrade system
(defn init-upgrades! []
  (reset! upgrade-state 
          {:registered-upgrades {}
           :active-upgrades {}})
  (doseq [[type modifiers] standard-upgrades]
    (register-upgrade! type modifiers)))