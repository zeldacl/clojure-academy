(ns cn.academy.block.matrix.energy)

(def ^:private DEFAULT_CAPACITY 100000)
(def ^:private DEFAULT_TRANSFER 1000)
(def ^:private CORE_MULTIPLIER 2.0)
(def ^:private PLATE_MULTIPLIER 1.5)

(defprotocol IMatrixEnergy
  (receive-energy [this amount simulate])
  (extract-energy [this amount simulate])
  (get-energy-stored [this])
  (get-energy-capacity [this])
  (get-bandwidth [this])
  (can-receive? [this])
  (can-extract? [this]))

(defrecord MatrixEnergy [matrix state energy-atom]
  IMatrixEnergy
  (receive-energy [this amount simulate]
    (when (can-receive? this)
      (let [capacity (get-energy-capacity this)
            stored (get-energy-stored this)
            space (- capacity stored)
            accept-amount (min amount space)]
        (when (pos? accept-amount)
          (when-not simulate
            (swap! energy-atom update :stored + accept-amount))
          accept-amount))))
  
  (extract-energy [this amount simulate]
    (when (can-extract? this)
      (let [stored (get-energy-stored this)
            extract-amount (min amount stored)]
        (when (pos? extract-amount)
          (when-not simulate
            (swap! energy-atom update :stored - extract-amount))
          extract-amount))))
  
  (get-energy-stored [_]
    (:stored @energy-atom))
  
  (get-energy-capacity [this]
    (let [core-level (:core-level @state)
          plate-count (:plate-count @state)]
      (* DEFAULT_CAPACITY 
         (Math/pow CORE_MULTIPLIER core-level)
         (Math/pow PLATE_MULTIPLIER plate-count))))
  
  (get-bandwidth [this]
    (* DEFAULT_TRANSFER 
       (Math/pow CORE_MULTIPLIER (:core-level @state))))
  
  (can-receive? [_]
    (:formed? @state))
  
  (can-extract? [_]
    (:formed? @state)))

(defn create-energy
  "Create new matrix energy handler"
  [matrix state]
  (->MatrixEnergy matrix
                  state
                  (atom {:stored 0})))