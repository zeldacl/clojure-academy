(ns cn.academy.registration
  (:require [mcmod.protocols :refer :all]
            [mcmod.registry :as mcr]
            [mcmod.util :as util]
            [clojure.tools.logging :as log])
  (:import [java.util UUID]))

(def MOD-ID "acmod")

;; Registry instance that will hold all mod content
(def registry (atom nil))

;; Initialize registry on first use
(defn get-registry []
  (when (nil? @registry)
    (reset! registry (mcr/create-registry)))
  @registry)

;; Block registration helpers
(defn register-block! [block-id block-def]
  (let [reg (get-registry)]
    (util/register-mod-block reg MOD-ID block-id block-def)))

(defn register-item! [item-id item-def]
  (let [reg (get-registry)]
    (util/register-mod-item reg MOD-ID item-id item-def)))

(defn register-tile-entity! [te-id te-def]
  (let [reg (get-registry)]
    (util/register-mod-tile-entity reg MOD-ID te-id te-def)))

;; Helper to create block with common properties
(defn create-block [id & {:keys [material hardness resistance light-level has-tile-entity]
                         :or {material :iron
                              hardness 3.0
                              resistance 5.0
                              light-level 0
                              has-tile-entity false}}]
  (reify IBlock
    (get-properties [_] 
      {:material material
       :hardness hardness
       :resistance resistance
       :light-level light-level
       :has-tile-entity has-tile-entity})
    
    (get-material [_] material)
    (get-hardness [_] hardness)
    (get-resistance [_] resistance)
    (get-light-level [_] light-level)
    
    (on-activated [_ pos data] true)
    (on-placed [_ pos data] nil)
    (on-removed [_ pos] nil)))

;; Registration function to be called during mod initialization
(defn init-registration! []
  (log/info "Initializing AcademyCraft registration")
  
  ;; Register all blocks defined in the block registry
  (doseq [[block-id block-def] (get-blocks)]
    (register-block! block-id block-def))
  
  ;; Register all items defined in the item registry  
  (doseq [[item-id item-def] (get-items)]
    (register-item! item-id item-def))
  
  ;; Register all tile entities
  (doseq [[te-id te-def] (get-tile-entities)]
    (register-tile-entity! te-id te-def)))