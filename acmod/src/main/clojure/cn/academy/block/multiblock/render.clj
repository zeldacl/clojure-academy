(ns cn.academy.block.multiblock.render
  (:require [cn.academy.block.multiblock.multiblock-base :as base]
            [cn.academy.block.multiblock.pattern :as pattern]
            [mcmod.render :as render]
            [mcmod.position :as position]
            [mcmod.block :as block]))

(defprotocol IStructureRenderer
  "Protocol for rendering multiblock structures"
  (render-structure! [this pos] "Render structure outline at position")
  (update-render! [this] "Update structure rendering")
  (get-render-state [this] "Get current render state"))

(defrecord MultiblockStructureRenderer [state-atom pattern]
  IStructureRenderer
  (render-structure! [_ pos]
    (when-let [blocks (:blocks @state-atom)]
      (doseq [[offset type] blocks]
        (let [world-pos (position/add pos offset)]
          (render/render-block-overlay! world-pos type)))))
  
  (update-render! [this]
    (swap! state-atom update :render-tick inc)
    (when (zero? (mod (:render-tick @state-atom) 20))
      (render-structure! this (:origin @state-atom))))
  
  (get-render-state [_]
    @state-atom))

(defrecord StructureValidationRenderer [state-atom validator]
  IStructureRenderer
  (render-structure! [_ pos]
    (let [blocks (pattern/get-pattern-blocks validator)
          valid? (pattern/matches-pattern? validator blocks)]
      (doseq [[offset type] blocks]
        (let [world-pos (position/add pos offset)
              overlay-type (if valid? :valid :invalid)]
          (render/render-block-overlay! world-pos type overlay-type)))))
  
  (update-render! [this]
    (swap! state-atom update :render-tick inc)
    (when (zero? (mod (:render-tick @state-atom) 20))
      (render-structure! this (:origin @state-atom))))
  
  (get-render-state [_]
    @state-atom))

(defn create-structure-renderer
  "Create a new structure renderer"
  [pattern]
  (->MultiblockStructureRenderer 
    (atom {:render-tick 0
           :origin nil
           :blocks []})
    pattern))

(defn create-validation-renderer
  "Create a new validation renderer"
  [validator]
  (->StructureValidationRenderer
    (atom {:render-tick 0
           :origin nil})
    validator))

(defn set-render-origin!
  "Set origin point for structure rendering"
  [renderer pos]
  (swap! (:state-atom renderer) assoc :origin pos))

(defn clear-render!
  "Clear structure rendering"
  [renderer]
  (swap! (:state-atom renderer) assoc 
         :origin nil
         :blocks []))