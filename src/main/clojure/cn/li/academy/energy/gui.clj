(ns cn.li.academy.energy.gui
  (:use [cn.li.forge-api.gui :only [defguihandler defcontainer]])
  (:use [cn.li.forge-api.utils :only [setfield getfield]])
  (:use [cn.li.academy.energy.tileentitis :only [tile-entity-node supported?]])
  (:import (net.minecraft.world World)
           (net.minecraft.entity.player EntityPlayer)
           (net.minecraft.inventory IInventory Slot Container)
           (net.minecraft.item ItemStack)))


(defn make-slot-IF-item [^IInventory inv slot x y]
  (proxy [Slot] [inv slot x y]
    (isItemValid [^ItemStack stack]
      (and stack (supported? stack)))))

(let [step 18
      post-init (fn [^Container this player-inventory tile-entity]
                  (.addSlotToContainer this (make-slot-IF-item tile-entity 0 38 71))
                  (.addSlotToContainer this (make-slot-IF-item tile-entity 1 78 71))
                  (doseq [i (range 9)]
                    (.addSlotToContainer this (Slot. player-inventory i (+ 8 (* i step)) 153)))
                  (doseq [i (range 1 4) j (range 9)]
                    (.addSlotToContainer this (Slot. player-inventory (+ (* (- 4 i) 9) j) (+ 8 (* j step)) (- 149 (* i step))))))]
  (defcontainer container-node :post-init post-init))

(defn container-node-transferStackInSlot [this ^EntityPlayer player id]
  (let [^Slot slot (.get (.inventorySlots this) id)]
    (when (and slot (.getHasStack slot))
      (let [stack1 (.getStack slot)
            stack (.copy stack1)
            merge-success (if (< id 2)
                            (.mergeItemStack this stack1 2 (.size (.-inventorySlots this)) true)
                            (.mergeItemStack this stack1 0 2 false))]
        (when merge-success
          (if (= (.-stackSize stack1) 0)
            (.putStack slot nil)
            (.onSlotChanged slot))
          stack)))))

(defn container-node-canInteractWith [this ^EntityPlayer player]
  (let [te (getfield this :tile-entity)]
    (< (.getDistanceSq player (+ (.-xCoord te) 0.5) (+ (.-yCoord te) 0.5) (+ (.-zCoord te) 0.5)) 64)))




(let [get-server-container (fn [id ^EntityPlayer player ^World world x y z]
                             (if-let [te (.getTileEntity world x y z)]
                               (if (instance? tile-entity-node te)
                                 (construct-proxy container-node (.-inventory player) te)
                                 )))
      get-client-container (fn [id player world x y z]
                             (if-let [c (get-server-container id player world x y z)]
                               (print "open gui")
                               ;(construct-proxy GuiNode c)
                               ))]
  (defguihandler node-gui-handler get-server-container get-client-container))
