(ns cn.academy.block.multiblock.render
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.block.multiblock.validation :as validation]
            [cn.academy.api.block :as block-api])
  (:import [net.minecraft.client.renderer RenderType]
           [com.mojang.blaze3d.matrix MatrixStack]
           [com.mojang.blaze3d.vertex IVertexBuilder]
           [net.minecraft.util.math BlockPos]
           [net.minecraft.client.renderer BufferBuilder]
           [net.minecraft.client.renderer.vertex VertexFormat VertexFormatElement]
           [org.lwjgl.opengl GL11]))

(def ^:private STRUCTURE_VALID_COLOR [0.0 1.0 0.0 0.4])
(def ^:private STRUCTURE_INVALID_COLOR [1.0 0.0 0.0 0.4])
(def ^:private STRUCTURE_INCOMPLETE_COLOR [1.0 1.0 0.0 0.4])

(defprotocol IStructureRenderer
  "Protocol for structure rendering"
  (render-structure [this matrix-stack buffer] "Render structure outline")
  (render-hologram [this matrix-stack buffer pos] "Render block hologram")
  (should-render? [this] "Check if structure should render"))

(defrecord MultiblockStructureRenderer [state-atom validator]
  IStructureRenderer
  (render-structure [_ matrix-stack buffer]
    (let [blocks (get @state-atom :preview-blocks #{})
          color (if-let [error (validation/get-error validator blocks)]
                 STRUCTURE_INVALID_COLOR
                 (if (validation/validate validator blocks)
                   STRUCTURE_VALID_COLOR
                   STRUCTURE_INCOMPLETE_COLOR))] 
      (doseq [pos blocks]
        (let [x (.getX pos)
              y (.getY pos)
              z (.getZ pos)]
          (.pushPose matrix-stack)
          (.translate matrix-stack x y z)
          (render-block-outline buffer matrix-stack color)
          (.popPose matrix-stack)))))
  
  (render-hologram [_ matrix-stack buffer pos]
    (let [requirements (validation/get-requirements validator)
          preview-blocks (get @state-atom :preview-blocks #{})]
      (when-let [next-type (first (remove #(contains? preview-blocks %)
                                        requirements))]
        (.pushPose matrix-stack)
        (.translate matrix-stack 
                   (.getX pos) (.getY pos) (.getZ pos))
        (render-block-hologram buffer matrix-stack
                             STRUCTURE_INCOMPLETE_COLOR)
        (.popPose matrix-stack))))
  
  (should-render? [_]
    (pos? (count (:preview-blocks @state-atom)))))

(defn- render-block-outline
  "Render block outline with color"
  [buffer matrix color]
  (let [[r g b a] color
        builder (.getBuffer buffer RenderType/LINES)]
    ;; Render wireframe cube
    (doseq [[x1 y1 z1 x2 y2 z2] [[0 0 0 1 0 0]
                                 [0 0 0 0 1 0] 
                                 [0 0 0 0 0 1]
                                 [1 1 1 0 1 1]
                                 [1 1 1 1 0 1]
                                 [1 1 1 1 1 0]]]
      (.vertex builder matrix x1 y1 z1)
      (.color builder r g b a)
      (.endVertex builder)
      (.vertex builder matrix x2 y2 z2)
      (.color builder r g b a) 
      (.endVertex builder))))

(defn- render-block-hologram
  "Render semi-transparent block"
  [buffer matrix color]
  (let [[r g b a] color
        builder (.getBuffer buffer RenderType/TRANSLUCENT)]
    ;; Render cube faces
    (doseq [[x1 y1 z1 x2 y2 z2 x3 y3 z3 x4 y4 z4] 
            [;; Bottom face
             [0 0 0 1 0 0 1 0 1 0 0 1]
             ;; Top face 
             [0 1 0 1 1 0 1 1 1 0 1 1]
             ;; Front face
             [0 0 0 1 0 0 1 1 0 0 1 0]
             ;; Back face
             [0 0 1 1 0 1 1 1 1 0 1 1]
             ;; Left face
             [0 0 0 0 1 0 0 1 1 0 0 1]
             ;; Right face
             [1 0 0 1 1 0 1 1 1 1 0 1]]]
      (.vertex builder matrix x1 y1 z1)
      (.color builder r g b a)
      (.endVertex builder)
      (.vertex builder matrix x2 y2 z2) 
      (.color builder r g b a)
      (.endVertex builder)
      (.vertex builder matrix x3 y3 z3)
      (.color builder r g b a)
      (.endVertex builder)
      (.vertex builder matrix x4 y4 z4)
      (.color builder r g b a)
      (.endVertex builder))))

(defn- draw-outline
  "Draw a block outline at the given position"
  [buffer pos color alpha]
  (let [[r g b] color
        x (.getX pos)
        y (.getY pos)
        z (.getZ pos)]
    ;; Draw cube outline
    (doto buffer
      (.pos x y z)
      (.color r g b alpha)
      (.endVertex)
      (.pos (inc x) y z)
      (.color r g b alpha)
      (.endVertex)
      
      (.pos x y z)
      (.color r g b alpha) 
      (.endVertex)
      (.pos x (inc y) z)
      (.color r g b alpha)
      (.endVertex)
      
      (.pos x y z)
      (.color r g b alpha)
      (.endVertex)
      (.pos x y (inc z))
      (.color r g b alpha)
      (.endVertex)
      
      ;; Other edges...
      )))

(defn render-formation-preview
  "Render preview of multiblock formation"
  [blocks validator world partialTicks]
  (GL11/glPushMatrix)
  (GL11/glEnable GL11/GL_BLEND)
  (GL11/glBlendFunc GL11/GL_SRC_ALPHA GL11/GL_ONE_MINUS_SRC_ALPHA)
  (GL11/glDisable GL11/GL_TEXTURE_2D)
  (GL11/glDepthMask false)
  
  (let [buffer (BufferBuilder. 256)
        valid? (validation/validate validator blocks)
        color (if valid? [0.0 1.0 0.0] [1.0 0.0 0.0])
        alpha 0.4]
    
    (.begin buffer 7 (.POSITION_COLOR (VertexFormat.)))
    
    (doseq [pos (map #(.getPos %) blocks)]
      (draw-outline buffer pos color alpha))
    
    (.finish buffer)
    
    (when-let [error (and (not valid?)
                         (validation/get-error validator blocks))]
      ;; Render error message above structure
      (let [center (reduce (fn [acc pos]
                            (-> acc
                                (.add (.getX pos))
                                (.add (.getY pos))
                                (.add (.getZ pos))))
                          (BlockPos/ZERO)
                          (map #(.getPos %) blocks))
            pos-count (count blocks)]
        (.renderFloatingText world 
                           error
                           (/ (.getX center) pos-count)
                           (+ (/ (.getY center) pos-count) 2)
                           (/ (.getZ center) pos-count)
                           0xFFFFFF))))
  
  (GL11/glEnable GL11/GL_TEXTURE_2D)
  (GL11/glDisable GL11/GL_BLEND)
  (GL11/glDepthMask true)
  (GL11/glPopMatrix))

(defn register-renderers!
  "Register multiblock preview renderers"
  []
  ;; Register with Forge rendering system
  )