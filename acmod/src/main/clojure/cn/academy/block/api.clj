(ns cn.academy.block.api
  (:require [mcmod.protocols :refer [IModAPI]]
            [cn.academy.block.machine :as machine]
            [cn.academy.block.network :as network]
            [cn.academy.block.fluid :as fluid]
            [cn.academy.block.capability :as cap]
            [cn.academy.block.storage :as storage]
            [cn.academy.block.error :as error]
            [cn.academy.block.config :as config]
            [clojure.tools.logging :as log]))

;; API state tracking
(def api-state
  (atom {:providers {}
         :callbacks {}}))

;; API provider implementation
(defrecord BlockAPI [state-atom]
  IModAPI
  (register-provider! [_ id provider]
    (swap! state-atom assoc-in [:providers id] provider))
  
  (get-provider [_ id]
    (get-in @state-atom [:providers id]))
  
  (register-callback! [_ event callback]
    (swap! state-atom update-in [:callbacks event] 
           (fnil conj #{}) callback))
  
  (notify-event! [_ event data]
    (doseq [callback (get-in @state-atom [:callbacks event])]
      (try
        (callback data)
        (catch Exception e
          (log/error "Error in API callback:" (.getMessage e)))))))

;; API machine interface
(defprotocol IMachineAPI
  (get-machine [this id])
  (get-all-machines [this])
  (register-machine-type! [this type config])
  (create-machine! [this type pos]))

;; API network interface
(defprotocol INetworkAPI
  (get-network [this id])
  (get-all-networks [this])
  (connect-nodes! [this node1 node2])
  (disconnect-nodes! [this node1 node2]))

;; API fluid interface  
(defprotocol IFluidAPI
  (register-fluid! [this id properties])
  (get-fluid-handler [this block-pos])
  (transfer-fluid! [this from-pos to-pos amount]))

;; API implementations
(defrecord MachineAPIProvider []
  IMachineAPI
  (get-machine [_ id]
    (machine/get-machine id))
  
  (get-all-machines [_]
    (machine/get-active-machines))
  
  (register-machine-type! [_ type config]
    (machine/register-machine-type! type config))
  
  (create-machine! [_ type pos]
    (machine/create-machine! type pos)))

(defrecord NetworkAPIProvider []
  INetworkAPI
  (get-network [_ id]
    (network/get-network id))
  
  (get-all-networks [_]
    (network/get-active-networks))
  
  (connect-nodes! [_ node1 node2]
    (network/connect-nodes! node1 node2))
  
  (disconnect-nodes! [_ node1 node2]
    (network/disconnect-nodes! node1 node2)))

(defrecord FluidAPIProvider []
  IFluidAPI
  (register-fluid! [_ id properties]
    (fluid/register-fluid! id properties))
  
  (get-fluid-handler [_ block-pos]
    (fluid/get-handler block-pos))
  
  (transfer-fluid! [_ from-pos to-pos amount]
    (fluid/transfer! from-pos to-pos amount)))

;; API factory functions
(defn create-api []
  (->BlockAPI api-state))

(defn create-machine-api []
  (->MachineAPIProvider))

(defn create-network-api []
  (->NetworkAPIProvider))

(defn create-fluid-api []
  (->FluidAPIProvider))

;; Public API functions
(def ^:private api-instance (atom nil))

(defn get-api []
  (or @api-instance
      (reset! api-instance (create-api))))

(defn get-machine-api []
  (.get-provider (get-api) :machine))

(defn get-network-api []
  (.get-provider (get-api) :network))

(defn get-fluid-api []
  (.get-provider (get-api) :fluid))

;; API event helpers
(defn on-machine-created! [callback]
  (.register-callback! (get-api) :machine/created callback))

(defn on-machine-destroyed! [callback]
  (.register-callback! (get-api) :machine/destroyed callback))

(defn on-network-changed! [callback]
  (.register-callback! (get-api) :network/changed callback))

(defn on-fluid-transferred! [callback]
  (.register-callback! (get-api) :fluid/transferred callback))

;; API initialization
(defn init-api! []
  (let [api (get-api)]
    ;; Register API providers
    (.register-provider! api :machine (create-machine-api))
    (.register-provider! api :network (create-network-api))
    (.register-provider! api :fluid (create-fluid-api))
    
    ;; Set up event forwarding
    (machine/on-created! #(.notify-event! api :machine/created %))
    (machine/on-destroyed! #(.notify-event! api :machine/destroyed %))
    (network/on-changed! #(.notify-event! api :network/changed %))
    (fluid/on-transferred! #(.notify-event! api :fluid/transferred %))))

;; Block creation helpers
(defn create-block-state [id properties]
  {:id id
   :properties properties})

;; Re-export commonly used functions from mcmod
(def set-block-property! mcmod.block/set-block-property!)
(def get-block-property mcmod.block/get-block-property)
(def create-block! mcmod.block/create-block!)
(def register-block! mcmod.block/register-block!)
(def get-block mcmod.block/get-block)

;; Block state management helpers
(defn update-block-state! [world pos state-fn]
  (when-let [block (get-block world pos)]
    (let [current-state (get-block-property block)
          new-state (state-fn current-state)]
      (set-block-property! block new-state))))

(defn with-block-state [block property value]
  (assoc-in block [:properties property] value))