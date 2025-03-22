(ns cn.academy.block.block.block-developer
  (:require [cn.academy.protocols.block :as block-api]
            [clojure.tools.logging :as log]))

;; Define the developer block properties
(def developer-working (atom nil))

(defn init-properties! [block-properties]
  (reset! developer-working (.createBooleanProperty block-properties "working")))

;; Create the developer block
(defn create-developer []
  (let [factory @block-api/*forge-factory*
        material (-> factory
                    block-api/create-block-properties
                    (block-api/get-block-material "iron"))
        block-properties (block-api/create-block-properties factory)
        container (block-api/create-block-container factory material)]
    
    ;; Initialize properties if needed
    (when (nil? @developer-working)
      (init-properties! block-properties))
    
    ;; Set basic block properties
    (block-api/set-hardness! container 3.0)
    (block-api/set-harvest-level! container "pickaxe" 1)
    
    ;; Add the working property to track developer state
    (when-not (nil? @developer-working)
      (try
        (.addProperty container @developer-working)
        (catch Exception e
          (log/error "Failed to add property to developer block:" (.getMessage e)))))
    
    container))

;; Helper functions for block state handling
(defn is-working? [state]
  (when @developer-working
    (block-api/get-property state @developer-working)))

(defn set-working [state working]
  (when @developer-working
    (block-api/with-property state @developer-working working)))

;; Export the constructor function for Java interop
(gen-class
  :name cn.academy.block.block.BlockDeveloper$Factory
  :methods [^:static [create [] Object]
            ^:static [initProperties [Object] void]]
  :prefix "block-factory-")

(defn block-factory-create []
  (create-developer))

(defn block-factory-initProperties [block-properties]
  (init-properties! block-properties))