(ns cn.li.academy.energy.blocks.node
  (:require [cn.li.mcmod.blocks :refer [defblock instance-block]])
  (:require [cn.li.mcmod.utils :refer [get-tile-entity-at-world blockstate->block drop-inventory-items same-block? open-gui]])
  (:require [cn.li.academy.energy.tileentites.node :refer [set-placer]])
  (:import (net.minecraft.block Block BlockState ChestBlock)
           (net.minecraft.block.material Material)
           (net.minecraft.item ItemStack)
           (net.minecraft.entity LivingEntity)
           (net.minecraft.util.math BlockPos BlockRayTraceResult)
           (net.minecraft.world World IBlockReader)
           (net.minecraft.entity.player PlayerEntity)
           (net.minecraft.util Hand)))

;(defblock vvv :properties {:material      Material/ROCK})
(defblock block-node
  ;:container? true
  ;:states {:type :unknown
  ;         ;:connected [:true :false]
  ;         ;:energy    [0 1 2 3 4]
  ;         }
  ;:override {:create-new-tile-entity new-tile-block-entity
  ;           :on-block-activated     on-tile-block-click}
  :state-properties {
               :connected [:bool]
               :energy [:integer 0 4]
               }
  ;:registry-name ""
  :properties {;:creative-tab ""
               :material      Material/ROCK
               :hardness      (float 2.5)
               ;:step-sound Block/soundTypeStone

               :harvest-level ["pickaxe", 1]}
  :overrides {;:create-new-tile-entity new-tile-block-entity
              ;:on-block-activated     on-tile-block-click
              :onBlockPlacedBy  (fn [^World worldIn, ^BlockPos pos, ^BlockState state, ^LivingEntity placer, ^ItemStack stack]
                                  (when-let [tile (get-tile-entity-at-world worldIn pos)]
                                    (set-placer tile placer)))
              :onReplaced       (fn [^BlockState state, ^World worldIn, ^BlockPos pos, ^BlockState newState isMoving]
                                  (when-not (same-block? state newState)
                                    (let [this ^Block this]
                                      (drop-inventory-items worldIn pos this)
                                      (proxy-super onReplaced state worldIn pos newState isMoving))))
              :onBlockActivated (fn [^BlockState state, ^World worldIn, ^BlockPos pos, ^PlayerEntity player, ^Hand handIn, ^BlockRayTraceResult hit]
                                  (let [this ^Block this]
                                    (open-gui player state worldIn pos this)))
              :getContainer     (fn [^BlockState state, ^World worldIn, ^BlockPos pos]
                                  3)
              :hasTileEntity    (constantly true)
              :createTileEntity (fn [^BlockState state, ^IBlockReader world] 1)
              }


  ;:creative-tab CreativeTabs/tabBlock
  )

(instance-block block-node)



;As anyone who's started porting to 1.14.2 is probably aware by now, container and GUI creation has changed... quite a bit.  To summarise what I've gathered so far:
;
;Containers (more accurately: container types) are now registry objects and must be registered in a RegistryEvent.Register<ContainerType<?>> event handler.
;Container objects themselves must provide a constructor of the form MyContainer(int windowId, PlayerInventory inv, PacketBuffer extraData).  This will be used to instantiate the client-side container.
;Your container's constructor must call the super Container constructor with your registered container type and the windowId parameter (this int parameter is not used in any other way - just pass it along to the superclass constructor and forget about it)
;You can use the extraData parameter to pass... extra data! to your container: one option would be to provide another constructor taking whatever parameters you need, which you call server-side from your container provider (see below), and client-side from the "factory" constructor (having extracted and marshalled any extra data).
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

; https://github.com/Cadiboo/Example-Mod/blob/d828cd29685f7732cac6a2a8cd0f5cbfee5d6e88/src/main/java/io/github/cadiboo/examplemod/ModEventSubscriber.java#L106-L118


;https://www.minecraftforge.net/forum/topic/72927-1143-is-this-a-bad-way-to-teleport-a-player/
;https://www.minecraftforge.net/forum/topic/72804-114-displaying-and-rendering-the-view-of-a-remote-position/
;https://www.minecraftforge.net/forum/topic/72738-1143-how-to-make-a-block-like-the-cauldron/
;https://www.minecraftforge.net/forum/topic/72282-1142-lightingrendering-issues-with-custom-block-model/
;https://www.minecraftforge.net/forum/topic/72681-solved1143-how-would-i-override-remove-a-crafting-recipe-with-the-new-crafting-system/
;https://www.minecraftforge.net/forum/topic/72600-1143-solved-replacement-for-nbttagcompoundsettag/
;https://www.minecraftforge.net/forum/topic/72442-1132-blockstate-to-item-block-nbt1-override-hardness2-language3/
;https://www.minecraftforge.net/forum/topic/72379-solved-1143-problem-with-names-in-custom-creative-tab/
;https://www.minecraftforge.net/forum/topic/72381-1143-solved-rendering-texture-in-gui/
;https://www.minecraftforge.net/forum/topic/71717-solved-1142-register-entities-and-rendering/
;https://www.minecraftforge.net/forum/topic/72180-1142-solved-what-controls-appropriate-enchantments/
;https://www.minecraftforge.net/forum/topic/72116-1142-registering-tileentites/
;https://www.minecraftforge.net/forum/topic/71952-1142-creating-a-custom-inventory-capabilities/
;https://www.minecraftforge.net/forum/topic/71577-1142-containers-and-guis/
;https://www.minecraftforge.net/forum/topic/71649-1142-world-generation/
;https://gist.github.com/williewillus/353c872bcf1a6ace9921189f6100d09a#world-gen-changes
;https://www.minecraftforge.net/forum/topic/71531-1142-solved-how-does-worldsaveddata-work-now/
;https://www.minecraftforge.net/forum/topic/72657-1143-entity-renderer-not-rendering/
;https://www.minecraftforge.net/forum/topic/73248-1143-passing-a-location-with-a-tool-to-a-blockentity/
;https://www.minecraftforge.net/forum/topic/72593-unsolved-1142-gui-isnt-opening-on-right-click/
;https://www.minecraftforge.net/forum/topic/73020-solved1143custom-gui/
;https://www.minecraftforge.net/forum/topic/73018-solved114-how-to-change-a-loot-table-dynamically/
;https://www.minecraftforge.net/forum/topic/73013-1143-gui-class-replaced-custom-hud-help/
;https://www.minecraftforge.net/forum/topic/72985-1143-mousescrollevent-without-a-gui/
;https://www.minecraftforge.net/forum/topic/72915-1143-solved-problem-with-playerinteracteventrightclickblock-event/
;https://www.minecraftforge.net/forum/topic/71577-1142-containers-and-guis/
; You must override Block#fillStateContainer if your block has properties. Look at vanilla for examples.


;ChestBlock
; public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
;      if (state.getBlock() != newState.getBlock()) {
;         TileEntity tileentity = worldIn.getTileEntity(pos);
;         if (tileentity instanceof IInventory) {
;            InventoryHelper.dropInventoryItems(worldIn, pos, (IInventory)tileentity);
;            worldIn.updateComparatorOutputLevel(pos, this);
;         }
;
;         super.onReplaced(state, worldIn, pos, newState, isMoving);
;      }
;   }

;   public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
;      if (worldIn.isRemote) {
;         return true;
;      } else {
;         INamedContainerProvider inamedcontainerprovider = this.getContainer(state, worldIn, pos);
;         if (inamedcontainerprovider != null) {
;            player.openContainer(inamedcontainerprovider);
;            player.addStat(this.getOpenStat());
;         }
;
;         return true;
;      }
;   }

;(defblock test-block
;  :hardness 0.5
;  :creative-tab CreativeTabs/tabBlock
;  :light-level (float 1.0)
;  :step-sound Block/soundTypeStone)
