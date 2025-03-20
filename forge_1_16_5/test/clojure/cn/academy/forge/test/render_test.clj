(ns cn.academy.forge.test.render-test
  (:require [clojure.test :refer :all]
            [mcmod.protocols :refer :all]
            [cn.academy.forge.render :as render]
            [cn.academy.forge.particles :as particles])
  (:import [com.mojang.blaze3d.matrix MatrixStack]
           [net.minecraft.client.renderer IRenderTypeBuffer]
           [net.minecraft.client.renderer.texture TextureAtlasSprite]
           [net.minecraft.util ResourceLocation]))

(deftest test-model-loader
  (let [loader (render/create-model-loader)]
    
    (testing "Model registration"
      (let [model (render/create-model-geometry 
                   [[0 0 0] [1 1 1]]  ; vertices
                   [[0 1 2]]          ; faces
                   [[0 0] [1 1]])]    ; texture coords
        (register-model loader "test_model" model)
        (is (= model (load-model loader "test_model")))))
        
    (testing "Texture loading"
      (let [texture-loc (load-texture loader "textures/block/test.png")]
        (is (instance? ResourceLocation texture-loc))))))

(deftest test-render-state
  (let [matrix-stack (MatrixStack.)
        state (render/create-render-state matrix-stack)]
        
    (testing "Matrix transformations"
      (-> state
          (push-matrix)
          (translate 1 2 3)
          (rotate 45 0 1 0)
          (scale 2 2 2)
          (pop-matrix))
          
      (is (zero? (.. matrix-stack last pose m03)))  ; Translation reset
      (is (= 1.0 (.. matrix-stack last pose m00))) ; Scale reset
      )
      
    (testing "Texture binding"
      (let [texture (ResourceLocation. "test:texture")
            new-state (bind-texture state texture)]
        (is (= texture (:texture new-state)))))))

(deftest test-particle-system
  (let [world (reify net.minecraft.world.World)
        manager (particles/create-particle-manager world)]
        
    (testing "Particle effect creation"
      (let [effect (particles/create-particle-effect 
                    [{:type :test
                      :offset-x 0
                      :offset-y 1
                      :offset-z 0
                      :velocity [0 0.1 0]}] 
                    20)]
        (is (satisfies? IParticleEffect effect))
        (is (not (is-finished? effect)))
        
        ;; Test effect updates
        (update-effect effect)
        (is (= 19 @(:duration effect)))
        
        (dotimes [_ 19]
          (update-effect effect))
        (is (is-finished? effect))))
        
    (testing "Particle spawning"
      (let [particle-type (reify IParticleType
                           (get-id [_] "test:particle")
                           (create-particle [_ world x y z vx vy vz data]
                             (reify IParticle
                               (tick [_])
                               (render [_ _ _])
                               (is-alive? [_] true))))]
        
        (register-factory manager particle-type identity)
        (spawn-particle manager particle-type 0 1 0 0 0.1 0))))))