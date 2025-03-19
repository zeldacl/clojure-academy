(ns forge-impl.test.wireless-matrix-integration-test
  (:require [clojure.test :refer :all]
            [forge-impl.test.integration-utils :as test]
            [mcmod.capabilities :as cap]
            [cn.academy.blocks.wireless-matrix :refer [->WirelessMatrix]]
            [forge-impl.block-converter :as block-conv]
            [forge-impl.capability-wrapper :as cap-wrapper])
  (:import [net.minecraft.util.math BlockPos]))

(deftest test-wireless-matrix-forge-integration
  (test/with-integration-world [world]
    (testing "Block registration and placement"
      (let [mcmod-block (->WirelessMatrix)
            forge-block (block-conv/convert-to-forge-block mcmod-block)
            pos (BlockPos. 0 64 0)]
        
        ;; Place block in world
        (.setBlockState world pos (.getDefaultState forge-block))
        
        (testing "Energy capability interaction"
          (let [tile-entity (.getTileEntity world pos)
                energy-cap (test/get-energy-capability tile-entity)]
            
            ;; Test energy storage through Forge capability
            (is (= 100000 (.getMaxEnergyStored energy-cap)))
            (.receiveEnergy energy-cap 5000 false)
            (is (= 5000 (.getEnergyStored energy-cap)))
            
            ;; Test energy transfer between Forge blocks
            (let [target-pos (BlockPos. 1 64 0)]
              (.setBlockState world target-pos (.getDefaultState forge-block))
              (let [target-te (.getTileEntity world target-pos)
                    target-cap (test/get-energy-capability target-te)]
                
                ;; Verify energy transfer works through Forge capability system
                (.extractEnergy energy-cap 1000 false)
                (.receiveEnergy target-cap 1000 false)
                
                (is (= 4000 (.getEnergyStored energy-cap)))
                (is (= 1000 (.getEnergyStored target-cap))))))))))))