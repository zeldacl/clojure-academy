(ns forge-impl.jei.plugin
  (:import [mezz.jei.api IModPlugin JeiPlugin]
           [mezz.jei.api.registration IRecipeRegistration]
           [net.minecraft.util ResourceLocation]))

(gen-class
  :name forge_impl.jei.ClojureAcademyJEIPlugin
  :implements [mezz.jei.api.IModPlugin]
  :prefix "jei-"
  :methods [[^:public getPluginUid [] net.minecraft.util.ResourceLocation]]
  :annotations {mezz.jei.api.JeiPlugin {}})

(defn jei-getPluginUid [this]
  (ResourceLocation. "cljacademy" "jei_plugin"))

(defn jei-registerRecipes [this registration]
  ;; Add custom information pages for our blocks
  (let [info-page (.addInfoPages registration)]
    (.addInfoPage info-page
                 "wireless_matrix"
                 (net.minecraft.util.text.TranslationTextComponent.
                   "block.cljacademy.wireless_matrix")
                 (net.minecraft.util.text.StringTextComponent.
                   "A block that can store and transfer energy wirelessly.\n\nCapacity: 100,000 FE\nTransfer Rate: 1,000 FE/t"))))