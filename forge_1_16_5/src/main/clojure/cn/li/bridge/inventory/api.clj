(ns cn.li.bridge.inventory.api)

(defprotocol IInventory
  "Core inventory functionality"
  (get-size [this] "Get inventory size")
  (get-stack [this slot] "Get item stack in slot")
  (set-stack [this slot stack] "Set item stack in slot")
  (remove-stack [this slot amount] "Remove items from slot")
  (is-empty? [this] "Check if inventory is empty")
  (mark-dirty [this] "Mark inventory as changed"))

(defprotocol IItemHandler
  "Item handler functionality"
  (get-slots [this] "Get number of slots")
  (get-stack-in-slot [this slot] "Get stack in slot")
  (insert-item [this slot stack simulate] "Insert stack into slot")
  (extract-item [this slot amount simulate] "Extract items from slot"))

(defprotocol IInventoryCapability
  "Inventory capability functionality"
  (get-inventory [this] "Get inventory interface")
  (get-item-handler [this side] "Get item handler for side"))

(defn create-forge-inventory []
  nil) ;; Implement in bridge