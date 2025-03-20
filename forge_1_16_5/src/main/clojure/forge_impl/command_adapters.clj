(ns forge-impl.command-adapters
  (:require [mcmod.commands :refer [ICommandBuilder ICommandContext]])
  (:import [com.mojang.brigadier.builder LiteralArgumentBuilder ArgumentBuilder]
           [net.minecraft.command Commands CommandSource]))

(defrecord ForgeCommandBuilder [^ArgumentBuilder builder]
  ICommandBuilder
  (literal [_ name]
    (->ForgeCommandBuilder
      (LiteralArgumentBuilder/literal name)))
  
  (requires [_ predicate]
    (->ForgeCommandBuilder
      (.requires builder #(predicate (->ForgeCommandContext %)))))
  
  (executes [_ handler]
    (->ForgeCommandBuilder
      (.executes builder #(handler (->ForgeCommandContext %)))))
  
  (then [_ child]
    (->ForgeCommandBuilder
      (.then builder (:builder child)))))

(defrecord ForgeCommandContext [context]
  ICommandContext
  (get-source [_]
    (.getSource context))
  
  (get-input [_]
    (.getInput context)))

(defn create-command-builder []
  (->ForgeCommandBuilder nil))