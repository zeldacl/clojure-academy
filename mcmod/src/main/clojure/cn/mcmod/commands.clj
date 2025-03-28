(ns cn.mcmod.commands
  (:require [cn.mcmod.logging :as log]))

(defprotocol ICommand
  (get-name [this] "Get the command name")
  (get-permission-level [this] "Get required permission level")
  (execute [this context args] "Execute the command"))

(defprotocol ICommandBuilder
  (literal [this name] "Create a literal command argument")
  (requires [this predicate] "Add permission requirement")
  (executes [this handler] "Set command execution handler")
  (then [this child] "Add child command"))

(defprotocol ICommandContext
  (get-source [this] "Get command source")
  (get-input [this] "Get raw command input"))

(defn create-command-dispatcher []
  (atom {}))

(defn register-command [dispatcher command]
  (swap! dispatcher assoc (get-name command) command))

(defn build-command-tree [dispatcher]
  (log/with-error-logging
    (reduce (fn [builder [name command]]
              (let [literal-node (literal builder name)]
                (-> literal-node
                    (requires #(>= (get-permission-level (get-source %))
                                 (get-permission-level command)))
                    (executes #(execute command % {})))))
            (literal nil "cljacademy")
            @dispatcher)))
