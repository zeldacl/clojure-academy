(ns cn.academy.block.matrix-adapter
  (:require [cn.academy.block.matrix :as matrix]))

(defprotocol IBlockAdapter
  "Protocol for platform-specific block implementation"
  (create-block [this properties]
    "Create a block with given properties")
  (register-block! [this block block-id]
    "Register a block with the platform"))

(defprotocol ITileEntityAdapter
  "Protocol for platform-specific tile entity implementation"
  (create-tile-entity-type [this block]
    "Create a tile entity type for the block")
  (register-tile-entity! [this tile-type tile-id]
    "Register a tile entity type with the platform")
  (create-tile-entity-instance [this tile-type pos]
    "Create a tile entity instance"))

(defprotocol IWorldAdapter
  "Protocol for platform-specific world interaction"
  (get-block-pos [this x y z]
    "Create a block position")
  (get-block-state [this world pos]
    "Get block state at position")
  (set-block-state! [this world pos state]
    "Set block state at position")
  (get-tile-entity [this world pos]
    "Get tile entity at position")
  (schedule-tick [this world pos ticks]
    "Schedule a block tick"))

(defprotocol IPlayerAdapter
  "Protocol for platform-specific player interaction"
  (get-player-name [this player]
    "Get player's name")
  (can-break-block? [this player pos]
    "Check if player can break block")
  (send-message [this player message]
    "Send message to player"))

(defprotocol INbtAdapter
  "Protocol for platform-specific NBT handling"
  (create-compound []
    "Create a new NBT compound")
  (put-boolean [this key value]
    "Put boolean value")
  (put-int [this key value]
    "Put integer value")
  (put-string [this key value]
    "Put string value")
  (put-compound [this key compound]
    "Put compound value"))