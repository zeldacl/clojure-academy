(ns cn.academy.block.security
  (:require [cn.academy.block.error :as error]
            [cn.academy.block.env :as env]
            [clojure.tools.logging :as log]))

;; Security levels
(def security-levels
  {:none 0
   :basic 1
   :restricted 2
   :protected 3
   :owner-only 4})

;; Permission types
(def permissions
  #{:interact :configure :modify :break :upgrade})

;; Security state
(def security-state
  (atom {:block-owners {}
         :team-permissions {}
         :block-permissions {}}))

;; Permission checking
(defn has-permission? [player block permission]
  (let [block-id (:id block)
        owner (get-in @security-state [:block-owners block-id])
        security-level (get-in block [:config :security-level] :none)]
    (cond
      ;; Owner always has all permissions
      (= owner (mcmod.player/get-id player))
      true
      
      ;; Admin bypass
      (mcmod.player/is-op? player)
      true
      
      ;; Check security level
      :else
      (case security-level
        :none true
        :basic (contains? #{:interact :configure} permission)
        :restricted (contains? #{:interact} permission)
        :protected (and (contains? #{:interact} permission)
                       (mcmod.player/get-team player)
                       (= (mcmod.player/get-team player)
                          (mcmod.player/get-team (mcmod.player/get-player owner))))
        :owner-only false))))

;; Security enforcement
(defn check-permission! [player block permission]
  (error/with-safe-execution (:id block) :security
    (if (has-permission? player block permission)
      true
      (do
        (log/warn "Permission denied:" 
                 {:player (mcmod.player/get-name player)
                  :block (:type block)
                  :permission permission})
        false))))

;; Block ownership
(defn set-owner! [block player]
  (swap! security-state assoc-in 
         [:block-owners (:id block)]
         (mcmod.player/get-id player)))

(defn get-owner [block]
  (get-in @security-state [:block-owners (:id block)]))

;; Team permissions
(defn set-team-permission! [team block-type permission allowed?]
  (swap! security-state assoc-in
         [:team-permissions team block-type permission]
         allowed?))

;; Block specific permissions
(defn set-block-permission! [block player permission allowed?]
  (swap! security-state assoc-in
         [:block-permissions (:id block) (mcmod.player/get-id player) permission]
         allowed?))

;; Security event handlers
(defn handle-block-place! [block player]
  (set-owner! block player))

(defn handle-block-break! [block player]
  (when (check-permission! player block :break)
    (swap! security-state update :block-owners dissoc (:id block))
    true))

;; Initialize security system
(defn init-security! []
  (mcmod.events/register-handler! :block-place handle-block-place!)
  (mcmod.events/register-handler! :block-break handle-block-break!))