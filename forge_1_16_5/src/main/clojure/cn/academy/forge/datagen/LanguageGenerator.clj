(ns cn.academy.forge.datagen.LanguageGenerator
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core])
  (:import [net.minecraft.data DataGenerator]
           [net.minecraftforge.common.data LanguageProvider]))

(def en-us-translations
  {"block.acmod.wireless_node_basic" "Basic Wireless Node"
   "block.acmod.wireless_node_standard" "Standard Wireless Node"
   "block.acmod.wireless_node_advanced" "Advanced Wireless Node"
   "block.acmod.machine_block" "Machine Block"
   
   "item.acmod.energy_component" "Energy Component"
   "item.acmod.wireless_component" "Wireless Component"
   
   "itemGroup.acmod" "Academy Craft"
   
   "command.acmod.wireless.usage" "Usage: /wireless <connect|disconnect> <target>"
   "command.acmod.wireless.connect.success" "Successfully connected nodes"
   "command.acmod.wireless.disconnect.success" "Successfully disconnected nodes"
   "command.acmod.wireless.error.no_node" "No wireless node found at position"
   "command.acmod.wireless.error.too_far" "Target node is too far away"
   "command.acmod.wireless.error.max_connections" "Node has reached maximum connections"
   
   "gui.acmod.energy_stored" "Energy Stored: %d/%d RF"
   "gui.acmod.wireless.range" "Range: %d blocks"
   "gui.acmod.wireless.connections" "Connections: %d/%d"})

(def zh-cn-translations
  {"block.acmod.wireless_node_basic" "基础无线节点"
   "block.acmod.wireless_node_standard" "标准无线节点"
   "block.acmod.wireless_node_advanced" "高级无线节点"
   "block.acmod.machine_block" "机器方块"
   
   "item.acmod.energy_component" "能量组件"
   "item.acmod.wireless_component" "无线组件"
   
   "itemGroup.acmod" "科技艺术"
   
   "command.acmod.wireless.usage" "用法: /wireless <connect|disconnect> <目标>"
   "command.acmod.wireless.connect.success" "成功连接节点"
   "command.acmod.wireless.disconnect.success" "成功断开节点"
   "command.acmod.wireless.error.no_node" "指定位置没有找到无线节点"
   "command.acmod.wireless.error.too_far" "目标节点距离太远"
   "command.acmod.wireless.error.max_connections" "节点已达到最大连接数"
   
   "gui.acmod.energy_stored" "储存能量: %d/%d RF"
   "gui.acmod.wireless.range" "范围: %d 方块"
   "gui.acmod.wireless.connections" "连接数: %d/%d"})

(defn add-translations! [provider translations]
  (doseq [[key value] translations]
    (.add provider key value)))

(defrecord EnUsProvider [^LanguageProvider delegate]
  ILanguageProvider
  (add-translation [this key value]
    (.add delegate key value))
    
  (add-block [this block name]
    (.addBlock delegate block name))
    
  (add-item [this item name]
    (.addItem delegate item name)))

(defrecord ZhCnProvider [^LanguageProvider delegate]
  ILanguageProvider
  (add-translation [this key value]
    (.add delegate key value))
    
  (add-block [this block name]
    (.addBlock delegate block name))
    
  (add-item [this item name]
    (.addItem delegate item name)))

(defn generate-languages! [^DataGenerator generator mod-id]
  (let [en-us (->EnUsProvider (proxy [LanguageProvider] [generator mod-id "en_us"]))
        zh-cn (->ZhCnProvider (proxy [LanguageProvider] [generator mod-id "zh_cn"]))]
        
    ;; Add English translations
    (add-translations! en-us en-us-translations)
    
    ;; Add Chinese translations  
    (add-translations! zh-cn zh-cn-translations)))