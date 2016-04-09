(ns cn.li.academy.energy.client.ui
  (:require [forge-clj.client.ui :refer [defguicontainer]]))

(defguicontainer test-gui-container
                 :expose-fields {guiLeft {:get getGuiLeft}
                                 guiTop {:get getGuiTop}
                                 xSize {:get getXSize}
                                 ySize {:get getYSize}
                                 fontRendererObj {:get getFontRenderer}})

(with-prefix test-gui-container-
             (defn drawGuiContainerBackgroundLayer [^TestGuiContainer this ticks mouse-x mouse-y]
               (set-gl-color 1 1 1 1)
               (bind-texture "test-mod:textures/gui/test-gui.png")
               (.drawTexturedModalRect this (.getGuiLeft this) (.getGuiTop this) 0 0 (.getXSize this) (.getYSize this)))

             (defn drawGuiContainerForegroundLayer [^TestGuiContainer this mouse-x mouse-y]
               (let [display-name "Test Inventory"]
                 (.drawString (.getFontRenderer this) display-name (- 88 (/ (.getStringWidth (.getFontRenderer this) display-name) 2)) 6 (int 0x404040)))))

