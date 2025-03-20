(ns cn.academy.forge.test.adapter-test
  (:require [clojure.test :refer :all]
            [mcmod.protocols :refer :all]
            [cn.academy.forge.adapter :as adapter]
            [cn.academy.forge.network :as network]
            [cn.academy.forge.gui :as gui])
  (:import [net.minecraftforge.energy IEnergyStorage]
           [net.minecraftforge.items IItemHandler]
           [net.minecraft.item ItemStack]
           [net.minecraft.util Direction]))

(deftest test-energy-adapter
  (let [storage (adapter/->ForgeEnergyStorage (atom 0) 1000)
        forge-storage (adapter/->ForgeEnergyAdapter storage)]
        
    (testing "Energy adapter implements Forge interface"
      (is (instance? IEnergyStorage forge-storage)))
      
    (testing "Energy operations map correctly"
      (.receiveEnergy forge-storage 500 false)
      (is (= 500 (.getEnergyStored forge-storage)))
      
      (.extractEnergy forge-storage 200 false)
      (is (= 300 (.getEnergyStored forge-storage)))
      
      (is (= 1000 (.getMaxEnergyStored forge-storage)))
      (is (true? (.canExtract forge-storage)))
      (is (true? (.canReceive forge-storage))))))

(deftest test-item-handler-adapter
  (let [handler (adapter/->ForgeItemHandler (atom {0 nil 1 nil}) 2)
        forge-handler (adapter/->ForgeItemHandlerAdapter handler)]
        
    (testing "Item handler implements Forge interface" 
      (is (instance? IItemHandler forge-handler)))
      
    (testing "Item operations map correctly"
      (is (= 2 (.getSlots forge-handler)))
      
      (let [stack (ItemStack. Items/DIAMOND 1)]
        (.insertItem forge-handler 0 stack false)
        (is (= 1 (.. (.getStackInSlot forge-handler 0) getCount)))
        
        (.extractItem forge-handler 0 1 false)
        (is (.isEmpty (.getStackInSlot forge-handler 0)))))))

(deftest test-capability-provider
  (let [energy-storage (adapter/->ForgeEnergyStorage (atom 0) 1000)
        handler (adapter/->ForgeItemHandler (atom {}) 9)
        provider (adapter/->ForgeCapabilityProvider 
                  {IEnergyStorage energy-storage
                   IItemHandler handler})]
                   
    (testing "Capability resolution"
      (let [energy-cap (.getCapability provider 
                                      net.minecraftforge.energy.CapabilityEnergy/ENERGY 
                                      Direction/NORTH)]
        (is (some? energy-cap))
        (is (instance? IEnergyStorage energy-cap)))
        
      (let [item-cap (.getCapability provider
                                    net.minecraftforge.items.CapabilityItemHandler/ITEM_HANDLER_CAPABILITY
                                    Direction/NORTH)]
        (is (some? item-cap))
        (is (instance? IItemHandler item-cap))))))

(deftest test-registry-bridge
  (let [bridge (adapter/create-registry-bridge "acmod")]
    (testing "Block registration"
      (let [block (reify IBlock
                    (get-properties [_] 
                      {:material :iron :hardness 3.0})
                    (on-placed [_ _ _ _])
                    (on-broken [_ _ _])
                    (on-activated [_ _ _ _ _]))
            registered (register-block bridge "test_block" block)]
        (is (some? registered))
        (is (instance? net.minecraft.block.Block registered))))
        
    (testing "Item registration"
      (let [item (reify IItem
                   (get-properties [_]
                     {:max-stack-size 16})
                   (get-max-stack-size [_] 16))
            registered (register-item bridge "test_item" item)]
        (is (some? registered))
        (is (instance? net.minecraft.item.Item registered))))))