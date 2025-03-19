(ns cn.academy.test.wireless-matrix-test
  (:require [clojure.test :refer :all]
            [mcmod.test-utils :as test]
            [mcmod.capabilities :as cap]
            [cn.academy.blocks.wireless-matrix :refer [->WirelessMatrix]]
            [cn.academy.tile-entities.wireless-matrix-te :refer [->WirelessMatrixTE]]
            [cn.academy.energy.transfer-handler :as energy]))

(deftest test-wireless-matrix-block
  (testing "Block properties"
    (let [block (->WirelessMatrix)]
      (is (= 4.0 (get-hardness block)))
      (is (= 20.0 (get-resistance block)))
      (is (= 7 (get-light-level block)))))
  
  (testing "Energy capability"
    (let [block (->WirelessMatrix)]
      (is (cap/has-capability? block "forge:energy" nil))
      (let [storage (cap/get-capability block "forge:energy" nil)]
        (is (= 100000 (cap/get-max-energy-stored storage)))
        (is (= 1000 (cap/receive-energy storage 1000 false)))
        (is (= 1000 (cap/get-energy-stored storage)))))))

(deftest test-wireless-matrix-te
  (test/with-test-world [world]
    (let [pos (test/create-mock-pos 0 0 0)
          te (->WirelessMatrixTE)]
      (test/set-tile-entity world pos te)
      
      (testing "Energy storage and transfer"
        (let [storage (cap/get-capability te "forge:energy" nil)]
          (cap/receive-energy storage 5000 false)
          (is (= 5000 (cap/get-energy-stored storage)))
          
          ;; Test energy transfer to nearby block
          (let [target-pos (test/create-mock-pos 1 0 0)
                target-te (->WirelessMatrixTE)]
            (test/set-tile-entity world target-pos target-te)
            (energy/transfer-energy te target-te 1000)
            
            (is (= 4000 (cap/get-energy-stored storage)))
            (is (= 1000 (cap/get-energy-stored 
                         (cap/get-capability target-te "forge:energy" nil)))))))))))