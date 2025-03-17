(ns cn.academy.block.matrix-sync
  (:require [cn.academy.block.matrix :as matrix]
            [cn.academy.block.matrix-state :as state]
            [cn.academy.block.matrix-particles :as particles]))

(defprotocol IMatrixSync
  (sync-state! [this])
  (handle-state-update [this data])
  (sync-particles! [this particle-type])
  (handle-particle-spawn [this data]))

(defrecord MatrixSyncHandler [matrix state particle-system]
  IMatrixSync
  (sync-state! [_]
    {:type :matrix-state
     :data {:core-level (matrix/get-core-level matrix)
            :plate-count (matrix/get-plate-count matrix)
            :state (state/serialize-state state)}})
  
  (handle-state-update [_ {:keys [core-level plate-count state]}]
    (matrix/set-core-level! matrix core-level)
    (matrix/set-plate-count! matrix plate-count)
    (state/deserialize-state state state))
  
  (sync-particles! [_ particle-type]
    (let [pos (matrix/get-position matrix)
          particle-fn (case particle-type
                       :core (particles/spawn-core-particles particle-system pos)
                       :plates (particles/spawn-plate-particles particle-system pos (matrix/get-plate-count matrix))
                       :shield (particles/spawn-shield-particles particle-system pos))]
      {:type :matrix-particles
       :data {:particle-type particle-type
              :pos pos}}))
  
  (handle-particle-spawn [_ {:keys [particle-type pos]}]
    (case particle-type
      :core (particles/spawn-core-particles particle-system pos)
      :plates (particles/spawn-plate-particles particle-system pos (matrix/get-plate-count matrix))
      :shield (particles/spawn-shield-particles particle-system pos))))

(defn create-sync-handler [matrix state particle-system]
  (->MatrixSyncHandler matrix state particle-system))