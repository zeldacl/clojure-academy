(ns cn.academy.block.lifecycle
  (:require [cn.academy.block.error :as error]
            [cn.academy.block.stats :as stats]
            [cn.academy.block.persistence :as persist]
            [cn.academy.block.resource :as resource]
            [cn.academy.block.network :as network]
            [clojure.tools.logging :as log]))

;; Lifecycle states
(def lifecycle-states
  #{:uninitialized :initializing :active :suspended :error :cleanup})

;; Lifecycle tracking
(def lifecycle-state
  (atom {:block-states {}
         :transitions {}
         :last-update {}}))

;; Lifecycle protocols
(defprotocol ILifecycle
  (initialize! [this])
  (activate! [this])
  (suspend! [this])
  (resume! [this])
  (cleanup! [this]))

;; State transitions
(defn- can-transition? [from to]
  (case [from to]
    [:uninitialized :initializing] true
    [:initializing :active] true
    [:active :suspended] true
    [:suspended :active] true
    [:suspended :cleanup] true
    [:active :cleanup] true
    [:error :cleanup] true
    false))

(defn transition! [block new-state]
  (let [block-id (:id block)
        current-state (get-in @lifecycle-state [:block-states block-id] :uninitialized)]
    (when (can-transition? current-state new-state)
      (swap! lifecycle-state 
             (fn [state]
               (-> state
                   (assoc-in [:block-states block-id] new-state)
                   (update-in [:transitions block-id] 
                             #(conj (or % []) 
                                   {:from current-state
                                    :to new-state
                                    :timestamp (System/currentTimeMillis)}))
                   (assoc-in [:last-update block-id] 
                            (System/currentTimeMillis)))))
      true)))

;; Lifecycle implementation
(defrecord BlockLifecycle [block hooks]
  ILifecycle
  (initialize! [_]
    (error/with-safe-execution (:id block) :lifecycle
      (when (transition! block :initializing)
        (try
          (when-let [init-fn (:initialize hooks)]
            (init-fn block))
          (transition! block :active)
          (log/info "Block initialized:" (:id block))
          true
          (catch Exception e
            (log/error "Failed to initialize block:" (:id block) (.getMessage e))
            (transition! block :error)
            false)))))
  
  (activate! [_]
    (error/with-safe-execution (:id block) :lifecycle
      (when (and (= (get-in @lifecycle-state [:block-states (:id block)])
                    :suspended)
                 (transition! block :active))
        (when-let [activate-fn (:activate hooks)]
          (activate-fn block))
        true)))
  
  (suspend! [_]
    (error/with-safe-execution (:id block) :lifecycle
      (when (and (= (get-in @lifecycle-state [:block-states (:id block)])
                    :active)
                 (transition! block :suspended))
        (when-let [suspend-fn (:suspend hooks)]
          (suspend-fn block))
        (persist/save-block-data! block)
        true)))
  
  (resume! [this]
    (activate! this))
  
  (cleanup! [_]
    (error/with-safe-execution (:id block) :lifecycle
      (when (transition! block :cleanup)
        (try
          (when-let [cleanup-fn (:cleanup hooks)]
            (cleanup-fn block))
          (persist/save-block-data! block)
          (swap! lifecycle-state 
                 #(-> %
                      (update :block-states dissoc (:id block))
                      (update :transitions dissoc (:id block))
                      (update :last-update dissoc (:id block))))
          true
          (catch Exception e
            (log/error "Failed to cleanup block:" (:id block) (.getMessage e))
            false))))))

;; Lifecycle management
(defn create-lifecycle! [block & {:as hooks}]
  (->BlockLifecycle block hooks))

;; Default lifecycle hooks
(def default-hooks
  {:initialize (fn [block]
                (resource/init-resources! block)
                (network/optimize-update! block :state @(:state block)))
   
   :activate (fn [block]
               (stats/track-block-activation! block))
   
   :suspend (fn [block]
              (stats/track-block-suspension! block))
   
   :cleanup (fn [block]
              (resource/cleanup-resources! block)
              (network/invalidate-cache! (:id block)))})

;; Lifecycle querying
(defn get-block-state [block]
  (get-in @lifecycle-state [:block-states (:id block)] :uninitialized))

(defn is-active? [block]
  (= (get-block-state block) :active))

(defn get-uptime [block]
  (when-let [last-update (get-in @lifecycle-state [:last-update (:id block)])]
    (- (System/currentTimeMillis) last-update)))

;; Initialize lifecycle system
(defn init-lifecycle! []
  (mcmod.events/register-handler! :block-place
    (fn [block]
      (let [lifecycle (create-lifecycle! block default-hooks)]
        (initialize! lifecycle))))
  
  (mcmod.events/register-handler! :block-break
    (fn [block]
      (when-let [lifecycle (->BlockLifecycle block default-hooks)]
        (cleanup! lifecycle)))))