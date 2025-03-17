(ns cn.li.mcmod.client.colors
  (:import (com.mojang.blaze3d.platform GlStateManager$Color)))

(defn from-float [r, g, b, a]
  {:r r :g g :b b :a a})

(defn mono-blend [luminance, alpha]
  (from-float luminance alpha luminance alpha))
