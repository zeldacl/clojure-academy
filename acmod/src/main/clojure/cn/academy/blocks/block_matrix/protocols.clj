(ns cn.academy.blocks.block-matrix.protocols
  "Centralized protocols for the wireless matrix block.
   This file defines all interfaces used across the block-matrix module.")

;;; Core Matrix Interfaces

(defprotocol IMatrixState
  "Manages the state of a wireless matrix block"
  (is-active? [this] "Check if matrix is active")
  (set-active [this active?] "Set active state")
  (get-core-level [this] "Get the level of installed core")
  (get-plate-count [this] "Get count of installed plates")
  (is-formed? [this] "Check if matrix has necessary components")
  (can-form? [this] "Check if matrix can form with current components")
  (save-state [this] "Get serializable state data")
  (load-state [this data] "Load serialized state data"))

(defprotocol IMatrixSecurity
  "Security management for matrix access control"
  (get-owner [this] "Get owner ID")
  (set-owner [this owner-id] "Set owner ID")
  (can-interact? [this player] "Check if player can interact with matrix")
  (set-password [this password] "Set network password")
  (get-password [this] "Get network password")
  (validate-password [this password] "Validate a password"))

(defprotocol IMatrixEnergy
  "Energy management for the matrix"
  (get-energy [this] "Get current energy")
  (set-energy [this amount] "Set energy to amount")
  (add-energy [this amount] "Add energy, returns amount added")
  (remove-energy [this amount] "Remove energy, returns amount removed")
  (get-energy-capacity [this] "Get maximum energy capacity")
  (get-transfer-rate [this] "Get energy transfer rate"))

(defprotocol IMatrixNetwork
  "Network management for the matrix"
  (join-network [this network-id password] "Join a network")
  (leave-network [this] "Leave current network")
  (is-connected? [this] "Check if connected to network")
  (get-network-id [this] "Get current network ID")
  (get-network-range [this] "Get network range")
  (get-connected-nodes [this] "Get connected network nodes") 
  (can-connect-node? [this node-pos] "Check if node can connect"))

(defprotocol IMatrixInventory
  "Inventory management for the matrix"
  (get-core-item [this] "Get installed core item")
  (set-core-item [this item] "Set core item")
  (get-core-level [this] "Get core level")
  (get-plate-items [this] "Get plate items")
  (add-plate-item [this item] "Add plate item")
  (remove-plate-item [this] "Remove plate item") 
  (get-plate-count [this] "Get plate count")
  (is-valid-core? [this item] "Check if item is valid core")
  (is-valid-plate? [this item] "Check if item is valid plate"))

;;; Extended Functionality

(defprotocol IMatrixRenderer
  "Rendering for the matrix block"
  (render-block [this world pos partial-ticks] "Render the block")
  (render-core [this] "Render the core")
  (render-plates [this] "Render the plates")
  (get-render-state [this] "Get current render state"))

(defprotocol IMatrixGui
  "GUI handling for the matrix"
  (create-container [this player] "Create container for GUI")
  (create-gui-elements [this width height] "Create GUI elements")
  (handle-button-event [this button-id] "Handle GUI button press"))

(defprotocol IMatrixEvents
  "Event handling for the matrix"
  (handle-event [this event-type & args] "Handle any matrix event")
  (subscribe-to-event [this event-type callback] "Subscribe to event")
  (unsubscribe-from-event [this event-type callback] "Unsubscribe from event"))

(defprotocol IMatrixConfig
  "Configuration for the matrix"
  (get-config [this] "Get full configuration")
  (get-config-value [this path default] "Get configuration value with optional default")
  (set-config-value! [this path value] "Set configuration value")
  (save-config [this] "Save configuration to disk")
  (load-config [this] "Load configuration from disk"))