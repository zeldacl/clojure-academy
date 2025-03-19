(ns cn.li.bridge.capability.api)

(defprotocol ICapabilityProvider
  "Capability provider functionality"
  (get-capability [this cap side] "Get capability for side")
  (invalidate-capabilities [this] "Invalidate all capabilities"))

(defprotocol ICapabilityManager
  "Capability management functionality"
  (register-capability [this cap-type] "Register new capability type")
  (get-capability-instance [this cap-type] "Get capability instance"))

(defn register-core-capabilities [bridge]
  nil) ;; Implement in bridge