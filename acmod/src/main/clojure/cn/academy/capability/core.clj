(ns cn.academy.capability.core)

(defprotocol ICapabilityProvider
  "Core capability functionality"
  (get-capabilities [this] "Get all capabilities for this object")
  (get-capability [this type side] "Get capability by type and side")
  (invalidate-capabilities! [this] "Invalidate all capabilities"))

(defprotocol ICapabilityType
  "Capability type"
  (get-type-id [this] "Get capability type identifier"))

(defprotocol ICapabilityHandler
  "Capability handler"
  (handle [this operation & args] "Handle capability operation"))

(defn create-provider [& {:keys [capabilities-fn]}]
  (let [caps-atom (atom {})]
    (reify ICapabilityProvider 
      (get-capabilities [_]
        (when capabilities-fn
          (capabilities-fn)))
      
      (get-capability [_ type side]
        (when-let [caps (get-capabilities _)]
          (get caps type)))
      
      (invalidate-capabilities! [_]
        (reset! caps-atom {})))))