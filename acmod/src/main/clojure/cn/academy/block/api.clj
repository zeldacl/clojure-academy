(ns cn.academy.block.api
  (:require [mcmod.protocols :refer [IModAPI IBlockEntity]]
            [cn.academy.block.machine.core :as machine]
            [cn.academy.block.multiblock.core :as multiblock]
            [cn.academy.block.event :as event]
            [cn.academy.block.error :as error]
            [clojure.tools.logging :as log]))

;; API state tracking
(def api-state
  (atom {:callbacks {}}))

;; API implementation
(defrecord BlockAPI [state-atom]
  IModAPI
  (register-callback! [_ event callback]
    (swap! state-atom update-in [:callbacks event] 
           (fnil conj #{}) callback))
  
  (notify-event! [_ event data]
    (doseq [callback (get-in @state-atom [:callbacks event])]
      (try
        (callback data)
        (catch Exception e
          (log/error "Error in API callback:" (.getMessage e))))))

  ;; Machine API methods
  IMachineAPI
  (register-machine-type! [_ type config]
    (machine/register-machine-type! type config))
  
  (create-machine! [_ type pos]
    (machine/create-machine! type :config {:position pos}))
  
  (get-machine [_ id]
    (machine/get-machine id))

  ;; Multiblock API methods 
  IMultiblockAPI
  (register-structure! [_ id blocks positions]
    (multiblock/register-pattern! id blocks positions))
  
  (validate-structure! [_ world pos pattern-id]
    (multiblock/validate-structure world pos pattern-id))
  
  (form-structure! [_ world pos pattern-id]
    (multiblock/try-form-structure! world pos pattern-id))

  ;; Block state API methods
  IBlockAPI
  (set-block-property! [_ block property value]
    (mcmod.block/set-block-property! block property value))
  
  (get-block-property [_ block property]
    (mcmod.block/get-block-property block property))
  
  (register-block-type! [_ id config]
    (mcmod.block/register-block-type! id config))
  
  (get-block-at [_ world pos]
    (mcmod.block/get-block-at world pos)))

;; API instance management
(def ^:private api-instance (atom nil))

(defn get-api []
  (or @api-instance
      (reset! api-instance (->BlockAPI api-state))))

;; Event subscription helpers
(defn on-machine-created! [callback]
  (.register-callback! (get-api) :machine/created callback))

(defn on-machine-destroyed! [callback]
  (.register-callback! (get-api) :machine/destroyed callback))

(defn on-structure-formed! [callback]
  (.register-callback! (get-api) :multiblock/formed callback))

(defn on-structure-broken! [callback]
  (.register-callback! (get-api) :multiblock/broken callback))

;; Block creation helpers
(defn create-block-state [id properties]
  {:id id
   :properties properties})

;; System initialization
(defn init-api! []
  (let [api (get-api)]
    ;; Set up event forwarding
    (event/register-handler :machine/created #(.notify-event! api :machine/created %))
    (event/register-handler :machine/destroyed #(.notify-event! api :machine/destroyed %))
    (event/register-handler :multiblock/formed #(.notify-event! api :multiblock/formed %))
    (event/register-handler :multiblock/broken #(.notify-event! api :multiblock/broken %))))