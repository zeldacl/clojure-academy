(ns cn.academy.block.registration
  (:require [cn.academy.block.material :as material]))

(defprotocol IBlockRegistration
  "Protocol for registering blocks in any Forge version"
  (register-block! [this block-def mod-id block-id]
    "Register a block with given properties and event handlers"))

(defprotocol IBlockFactory 
  "Protocol for creating blocks in any Forge version"
  (create-block [this properties event-handlers]
    "Create a block instance with given properties and event handlers"))

(defprotocol IBlockProperties
  "Protocol for handling block properties in any Forge version" 
  (apply-properties! [this target properties]
    "Apply properties to target block"))