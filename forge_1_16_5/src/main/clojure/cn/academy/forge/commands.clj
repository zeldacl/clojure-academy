(ns cn.academy.forge.commands
  (:require [mcmod.protocols :refer :all]
            [cn.academy.core :as core])
  (:import [com.mojang.brigadier CommandDispatcher]
           [com.mojang.brigadier.builder LiteralArgumentBuilder ArgumentBuilder]
           [com.mojang.brigadier.context CommandContext]
           [net.minecraft.command Commands CommandSource]))

(defrecord ForgeCommand [name permission-level handler]
  ICommand
  (get-name [this]
    name)
    
  (get-permission-level [this]
    permission-level)
    
  (execute [this context args]
    (handler context args)))

(defrecord ForgeCommandBuilder [delegate]
  ICommandBuilder
  (literal [this name]
    (assoc this :delegate 
           (LiteralArgumentBuilder/literal name)))
           
  (requires [this predicate]
    (update this :delegate #(.requires % predicate)))
    
  (executes [this handler]
    (update this :delegate #(.executes % handler)))
    
  (then [this child]
    (update this :delegate #(.then % (:delegate child)))))

(defrecord ForgeCommandContext [^CommandContext delegate]
  ICommandContext
  (get-source [this]
    (.getSource delegate))
    
  (get-input [this]
    (.getInput delegate)))

(defn register-command! [^CommandDispatcher dispatcher command]
  (let [builder (-> (LiteralArgumentBuilder/literal (get-name command))
                    (.requires #(>= (.getLevel ^CommandSource %) 
                                  (get-permission-level command)))
                    (.executes (reify com.mojang.brigadier.Command
                               (run [_ ctx]
                                 (execute command 
                                         (->ForgeCommandContext ctx)
                                         [])))))]
    (.register dispatcher builder)))

;; Utility functions for creating commands
(defn create-command [name permission-level handler]
  (->ForgeCommand name permission-level handler))

(defn create-command-builder []
  (->ForgeCommandBuilder nil))

;; Example command implementations
(defn create-wireless-node-command []
  (create-command 
    "wireless"
    2  ; Requires operator permission
    (fn [ctx args]
      (let [source (get-source ctx)
            world (.getLevel source)
            pos (.getBlockPos source)]
        ;; Execute wireless node command logic
        1))))  ; Return success

(defn create-debug-command []
  (create-command
    "acdebug"
    2
    (fn [ctx args]
      (let [source (get-source ctx)
            world (.getLevel source)]
        ;; Execute debug command logic  
        1))))

;; Command registration
(defn register-mod-commands! [dispatcher]
  (register-command! dispatcher (create-wireless-node-command))
  (register-command! dispatcher (create-debug-command)))