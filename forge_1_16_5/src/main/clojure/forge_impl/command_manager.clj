(ns forge-impl.command-manager
  (:require [mcmod.commands :as cmd]
            [mcmod.logging :as log]
            [cn.academy.commands.wireless-matrix-commands :as matrix-cmds]
            [cn.academy.commands.stats-command :as stats-cmd]
            [cn.academy.commands.reset-stats-command :as reset-cmd])
  (:import [net.minecraftforge.event.RegisterCommandsEvent]
           [net.minecraftforge.eventbus.api SubscribeEvent]
           [net.minecraftforge.fml.common Mod$EventBusSubscriber]))

(def command-dispatcher (cmd/create-command-dispatcher))

(defn register-mod-commands []
  (doto command-dispatcher
    (cmd/register-command (matrix-cmds/->CheckEnergyCommand))
    (cmd/register-command (matrix-cmds/->SetEnergyCommand))
    (cmd/register-command (stats-cmd/->ViewStatsCommand))
    (cmd/register-command (reset-cmd/->ResetStatsCommand))))

(defn handle-command-registration [^RegisterCommandsEvent event]
  (let [dispatcher (.getDispatcher event)
        command-tree (cmd/build-command-tree command-dispatcher)]
    (.register dispatcher command-tree)))

(gen-class
  :name forge_impl.CommandManager
  :prefix "manager-"
  :methods [[^{SubscribeEvent {}} registerCommands [net.minecraftforge.event.RegisterCommandsEvent] void]]
  :annotations {Mod$EventBusSubscriber 
               {:modid "cljacademy" 
                :bus Mod$EventBusSubscriber$Bus/FORGE}})

(defn manager-registerCommands [this event]
  (handle-command-registration event))