(ns cn.li.mcmod.ui
  (:require [cn.li.mcmod.utils :refer [get-fullname with-prefix construct]]
            [clojure.tools.logging :as log]
            [cn.li.mcmod.core :refer [defclass]]
            [clojure.string :as str])
  (:import (net.minecraftforge.common.extensions IForgeContainerType)
           (net.minecraft.inventory.container Container Slot)
           (net.minecraft.entity.player PlayerInventory)
           (net.minecraft.network PacketBuffer)
           (net.minecraftforge.items SlotItemHandler IItemHandler)
           (net.minecraftforge.items.wrapper InvWrapper)
           (net.minecraftforge.fml.network IContainerFactory)
           (net.minecraft.util ResourceLocation)
           (net.minecraft.util.text ITextComponent)
           (net.minecraft.client.gui.screen.inventory ContainerScreen)
           (com.mojang.blaze3d.platform GlStateManager GlStateManager$SourceFactor GlStateManager$DestFactor)))

;;; This fn allows calling any method, as long as it's the first with that name in getDeclaredMethods().
;;; Works even when the arguments are primitive types.
;
;(defn call-method
;  [obj method-name & args]
;  (let [m (first (filter (fn [x] (.. x getName (equals method-name)))
;                         (.. obj getClass getDeclaredMethods)))]
;    (. m (setAccessible true))
;    (. m (invoke obj (into-array Object args)))))
;
;
;;; This function comes from clojure.contrib.reflect. A version of it is also in https://github.com/arohner/clj-wallhack.
;;; It allows calling any method whose arguments have class types, but not primitive types.
;
;(defn call-method
;  "Calls a private or protected method.
;   params is a vector of classes which correspond to the arguments to the method
;   obj is nil for static methods, the instance object otherwise.
;   The method-name is given a symbol or a keyword (something Named)."
;  [klass method-name params obj & args]
;  (-> klass (.getDeclaredMethod (name method-name)
;                                (into-array Class params))
;      (doto (.setAccessible true))
;      (.invoke obj (into-array Object args))))

(defmacro defcontainerslot [name & args]
  (let [classdata (apply hash-map args)]
    `(do
       (defclass ~name SlotItemHandler ~classdata))))

(defcontainerslot slot-inv)


;(defmulti transfer-slot? (fn [from to]
;                           [(class from) (class to)]))
;
;(defmethod transfer-slot? :default [from to] true)

(defn add-range
  [^net.minecraft.inventory.container.Container container ^IItemHandler handler index x y amount dx]
  (let [index (int index)
        x (int x)
        y (int y)
        amount (int amount)
        dx (int dx)
        add-slot (fn [^long i ^long x ^long y]
                   (let [^Container container container]
                     ; fixme protected method
                     ;(def a (ref 0))
                     ; (def klass (class a))
                     ; (def m (.getDeclaredMethod klass "currentVal" (into-array Class [])))
                     ; (.setAccessible m true)
                     ; (.invoke m a (into-array []))
                     (.addSlot ^Container container ^Slot (construct slot-inv handler i x y)))
                   )]
    (dorun (map #(add-slot (+ %1 index), (+ (* %1 dx) x), y)) (range amount)))
  ;([^Container container ^IItemHandler player-inventory]
  ; (add-range container player-inventory 0 6 163 9 18))
  )

(defn add-box [^Container container ^IItemHandler handler index x y x-amount y-amount dx dy]
  (dorun (map #(add-range container handler (+ (* x-amount %1) index), x, (+ (* %1 dy) y), x-amount, dx)) (range y-amount)))

(defn map-player-inventory [container inventory]
  (let [x 6
        dx 18
        dy -18
        x-amount 9
        y-amount 4]
    (add-range container inventory 0 x 163 x-amount dx)
    (add-box container inventory x-amount x 159 x-amount y-amount dx dy)))


(defmacro defblockcontainer [name & args]
  (let [blockdata (apply hash-map args)
        class-name (symbol name)
        prefix (str name "-")
        name-ns (get blockdata :ns *ns*)
        fullname (get-fullname name-ns class-name)
        this-sym (with-meta 'this {:tag fullname})]
    `(do
       (gen-class
         :name ~fullname
         :prefix ~(symbol prefix)
         :extends ~Container
         :init ~'initialize
         :constructors {[~Integer/TYPE PlayerInventory PacketBuffer] [~Integer/TYPE PlayerInventory PacketBuffer]}
         :post-init ~'post-initialize
         :state 'data)
       (def ~class-name ~fullname)
       ;(def ~class-name (eval '~fullname))
       (import ~fullname)
       (with-prefix ~prefix
         (defn ~'initialize
           ([~'world-id ~'player-inventory ~'packet-buffer]
            [[~'world-id ~'player-inventory ~'packet-buffer]
             (atom {:player-inventory ~'player-inventory
                    :tileentity (.readBlockPos ~'packet-buffer)})]))
         (defn ~'post-initialize
           ([~this-sym]
            nil)
           ([~this-sym ~'world-id ~'player-inventory ~'packet-buffer]
            ;(log/info "^^^^^^^^^^^^^^^^^^" ~x ~'args)
            (map-player-inventory ~this-sym (InvWrapper. ~'player-inventory) )
            ;(.setRegistryName ~'obj ~registry-name)
           ))
         )
       )))

(defn create-container-type [container ^ResourceLocation registry-name]
  (let [factory (proxy [IContainerFactory] []
                  (create [window-id inv data]
                    (construct container window-id inv data)))
        container-type (IForgeContainerType/create factory)
        container-type (.setRegistryName container-type registry-name)]
    container-type))

(defmacro defcontainertype [name container registry-name]
  `(def ~name (create-container-type ~container ~registry-name)))
;IForgeContainerType.create(RPGInventoryContainer::new).setRegistryName("rpg_inventory")

(defmacro defcontainerscreen [name container & args]
  (let [blockdata (apply hash-map args)
        class-name (symbol name)
        prefix (str name "-")
        name-ns (get blockdata :ns *ns*)
        fullname (get-fullname name-ns class-name)
        this-sym (with-meta 'this {:tag fullname})
        ]
    `(do
       (gen-class
         :name ~fullname
         :prefix ~(symbol prefix)
         :extends ~ContainerScreen
         ;:init ~'initialize
         :constructors {[~(var-get (resolve container)) PlayerInventory ITextComponent] [~(var-get (resolve container)) PlayerInventory ITextComponent]}
         :post-init ~'post-initialize
         :state 'data)
       (with-prefix ~prefix
         ;(defn ~'aaa
         ;  ([~'world-id ~'player-inventory ~'packet-buffer]
         ;   [[~'world-id ~'player-inventory ~'packet-buffer]
         ;    (atom {:player-inventory ~'player-inventory
         ;           :tileentity (.readBlockPos ~'packet-buffer)})]))
         (defn ~'render [~'this ~'mouseX ~'mouseY ~'partialTicks]
           ;if(isSlotActive()) {
           ;            this.drawDefaultBackground();
           ;            super.drawScreen(a, b, c);
           ;            renderHoveredToolTip(a, b);
           ;        } else {
           ;            gui.resize(width, height);
           ;            this.drawDefaultBackground();
           ;            GL11.glEnable(GL11.GL_BLEND);
           ;            gui.draw(a, b);
           ;            GL11.glDisable(GL11.GL_BLEND);
           ;        }
           )
         ;(defn ~'drawGuiContainerForegroundLayer [~'this ~'mouseX ~'mouseY])
         (defn ~'drawGuiContainerBackgroundLayer [~'this ~'partialTicks ~'mouseX ~'mouseY]
           (GlStateManager/enableBlend)
           (GlStateManager/blendFunc GlStateManager$SourceFactor/SRC_ALPHA GlStateManager$DestFactor/ONE_MINUS_SRC_ALPHA)
           ;gui.resize(width, height);
           ;        gui.draw(var2, var3);
           )
         (defn ~'mouseClicked [~'this ~'par1 ~'par2 ~'par3]
           ;if(isSlotActive()) super.mouseClicked(par1, par2, par3);
           ;        gui.mouseClicked(par1, par2, par3);
           )
         (defn ~'mouseDragged [~'this ~'par1 ~'par2 ~'par3 ~'par4 ~'par5]
           ;if(isSlotActive()) super.mouseClickMove(mx, my, btn, time);
           ;        gui.mouseClickMove(mx, my, btn, time);
           )
         (defn ~'removed [~'this]
           ;super.removed();
           ;        gui.dispose();
           )
         (defn ~'keyPressed [~'this ~'par1 ~'par2 ~'par3]
           ;gui.keyTyped(ch, key);
           ;        if(containerAcceptsKey(key) || key == Keyboard.KEY_ESCAPE)
           ;            super.keyTyped(ch, key);
           )
         ))))

;(let [a (doto
;          (Block$Properties/create Material/AIR)
;          (.hardnessAndResistance 0.5 0.5)
;          (.sound SoundType/WOOD)
;          )])

;https://www.minecraftforge.net/forum/topic/76728-1144-guicontainer-for-player-no-tile-entity/

;https://www.minecraftforge.net/forum/topic/71577-1142-containers-and-guis/
;As anyone who's started porting to 1.14.2 is probably aware by now, container and GUI creation has changed... quite a bit.  To summarise what I've gathered so far:
;
;Containers (more accurately: container types) are now registry objects and must be registered in a RegistryEvent.Register<ContainerType<?>> event handler.
;Container objects themselves must provide a constructor of the form MyContainer(int windowId, PlayerInventory inv, PacketBuffer extraData).  This will be used to instantiate the client-side container.
;Your container's constructor must call the super Container constructor with your registered container type and the windowId parameter (this int parameter is not used in any other way - just pass it along to the superclass constructor and forget about it)
;You can use the extraData parameter to pass... extra data! to your container.  This PacketBuffer parameter is built server-side when you call NetworkHooks.openGui(); see below.
;Typically, your container will have (at least) two constructors; the factory constructor described above which is used for client-side construction, and a constructor taking whatever parameters you need to pass to set up your server-side container object.
;Container-based GUI's are now associated with a container type with the ScreenManager.registerFactory() method, which takes a registered ContainerType and a ScreenManager.IFactory to construct your GUI object. Call that in your client-side init code.
;ExtensionPoint.GUIFACTORY is gone, no longer needed, as is the old IGuiHandler system.  ScreenManager does all that work now.
;While there must be a one-to-one mapping from ContainerType to each GUI, also remember that you can happily re-use the same container class for multiple container types if you need to; just pass the container type as a parameter to your constructor and up along to the Container constructor.
;All container-based GUI's must provide a constructor taking(T, PlayerInventory, ITextComponent), where the generic T is the type of your container object.
;Container-based GUI objects are now generified (is that a word?) on the container's class, e.g. public class MyGui extends ContainerScreen<MyContainer>
;IInteractionObject is gone; instead your tile entity should implement INamedContainerProvider: the createMenu() method is where you create and return your server-side container object. (I believe getDisplayName() is only used to initialize the title field of your GUI object).
;To open a container-based GUI on the tile entity (server-side), call NetworkHooks.OpenGui(player, myTileEntity, pos) or player.openContainer(myTileEntity)
;That would typically be called from Block#onBlockActivated()
;If you want to create a container-based GUI for an item, create a class implementing INamedContainerProvider (a static inner class of the item makes sense, or perhaps an anonymous implementation), implementing createMenu() and getDisplayName() to create and return the container object as above.
;That would typically be called as something like NetworkHooks.OpenGui(player, new MyItemContainerProvider(stack), pos)  or player.openContainer(new MyItemContainerProvider(stack)) from Item#onItemRightClick() or Item#onItemUse()
;player.openContainer() can be used if you have no need to pass any extra data to the client, e.g. your GUI is just displaying container slots, like a chest.  But if your client GUI needs access to the client-side tile entity, use NetworkHooks.openGui() and pass at least the tile entity's blockpos so the client container can get the GUI object.
;Syncing any necessary TE data to the client-side TE needs to be done separately by whatever means you choose.
;Note that NetworkHooks.openGui() has a couple of variants to allow extra data to be passed to the client-side container object: a simple pos argument if you just need to pass the blockpos of your TE, or a more flexible Consumer<PacketBuffer> if you have more complex data to pass to the client.  The simple pos variant just calls the full-blooded Consumer<PacketBuffer> variant in any case.  It's this packet buffer that is received by the client-side container constructor.

;Bit of follow up since I understand a bit more now....
;
;To get extra data passed across to the client (like a TE blockpos, for example), Forge 26.0.16 adds an extra PacketBuffer parameter to the NetworkHooks.openGui() calls, and a corresponding parameter to the container factory constructor.
;Looks like NetworkHooks.openGui() remains the way to go for modded - I guess player.openContainer() is really for vanilla only?
;Update: player.openContainer() should be fine to use if you're just creating a GUI purely to display some container slots (and don't need direct access to the clientside tile entity), like a chest GUI for example.
;Typically, you'll have two or more constructors in your container objects - one "factory" constructor which is called by Minecraft client-side when a GUI is opened (and when the container type is registered during init), and one or more constructors of your choosing which create a container with any data you need to initialize them with.
;Those extra constructors would be called server-side from your INamedContainerProvider implementation, and client-side from your "factory" constructor, having extracted information from the extraData PacketBuffer.
