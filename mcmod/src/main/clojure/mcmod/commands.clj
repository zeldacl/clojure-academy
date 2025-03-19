(ns mcmod.commands
  (:require [mcmod.logging :as log])
  (:import [com.mojang.brigadier.builder LiteralArgumentBuilder]
           [com.mojang.brigadier CommandDispatcher]
           [net.minecraft.command Commands CommandSource]))

(defprotocol ICommand
  (get-name [this] "Get the command name")
  (get-permission-level [this] "Get required permission level")
  (execute [this context args] "Execute the command"))

(defn create-command-dispatcher []
  (atom {}))

(defn register-command [dispatcher command]
  (swap! dispatcher assoc (get-name command) command))

(defn build-command-tree [dispatcher]
  (log/with-error-logging
    (reduce (fn [builder [name command]]
              (.then builder
                    (.. (LiteralArgumentBuilder/literal name)
                        (requires #(>= (.hasPermissionLevel ^CommandSource %)
                                     (get-permission-level command)))
                        (executes #(execute command % {})))))
            (LiteralArgumentBuilder/literal "cljacademy")
            @dispatcher)))