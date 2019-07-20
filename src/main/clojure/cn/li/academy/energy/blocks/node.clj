(ns cn.li.academy.energy.blocks.node
  (:require [cn.li.mcmod.blocks :refer [defblock]])
  (:require [cn.li.mcmod.utils :refer [get-tile-entity-at-world]])
  (:require [cn.li.academy.energy.tileentites.node :refer [set-placer]])
  (:import (net.minecraft.block Block BlockState ChestBlock)
           (net.minecraft.block.material Material)
           (net.minecraft.item ItemStack)
           (net.minecraft.entity LivingEntity)
           (net.minecraft.util.math BlockPos)
           (net.minecraft.world World)))

(defblock block-node
  :container? true
  :states {:type :unknown
           ;:connected [:true :false]
           ;:energy    [0 1 2 3 4]
           }
  ;:override {:create-new-tile-entity new-tile-block-entity
  ;           :on-block-activated     on-tile-block-click}
  :properties {
               :connected [:bool]
               :energy [:integer 0 4]
               }
  :attributes {;:creative-tab ""
               :material      Material/ROCK
               :hardness      (float 2.5)
               ;:step-sound Block/soundTypeStone
               :registry-name ""
               :harvest-level ["pickaxe", 1]}
  :override {;:create-new-tile-entity new-tile-block-entity
             ;:on-block-activated     on-tile-block-click
             :on-block-placed-by (fn [this ^World worldIn, ^BlockPos pos, ^BlockState state, ^LivingEntity placer, ^ItemStack stack]
                                   (when-let [tile (get-tile-entity-at-world worldIn pos)]
                                     (set-placer tile placer)))
             :get-drops
             }


  ;:creative-tab CreativeTabs/tabBlock
  )
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
