(ns cn.academy.inventory.core)

(defprotocol IInventory
  "Core inventory functionality"
  (get-size [this] "Get inventory size")
  (get-stack [this slot] "Get item stack in slot")
  (set-stack [this slot stack] "Set item stack in slot")
  (get-max-stack-size [this] "Get maximum stack size")
  (is-valid-slot [this slot] "Check if slot index is valid")
  (is-empty? [this] "Check if inventory is empty")
  (is-valid-stack? [this slot stack] "Check if stack is valid for slot")
  (mark-dirty [this] "Mark inventory as changed"))

(defprotocol IItemHandler
  "Item handler functionality"
  (get-slots [this] "Get number of slots")
  (get-stack-in-slot [this slot] "Get stack in slot")
  (insert-item [this slot stack simulate?] "Insert stack into slot")
  (extract-item [this slot amount simulate?] "Extract items from slot")
  (get-slot-limit [this slot] "Get slot stack limit")
  (is-item-valid? [this slot stack] "Check if item valid for slot"))

(defprotocol IInventoryFactory 
  "Inventory factory functionality"
  (create-inventory [this size] "Create inventory with size")
  (load-from-nbt [this nbt] "Load inventory from NBT")
  (save-to-nbt [this] "Save inventory to NBT"))