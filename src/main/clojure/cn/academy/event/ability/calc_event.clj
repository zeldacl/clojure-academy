(ns cn.academy.event.ability.calc-event
  (:require [net.minecraftforge.common :refer [MinecraftForge]]
            [net.minecraftforge.fml.common.eventhandler :refer [Event]]))

(defn calc [evt]
  (MinecraftForge/EVENT_BUS/post evt)
  (:value evt))

(defrecord CalcEvent [value])

(defrecord PlayerCalcEvent [player value] CalcEvent)

(defrecord MaxCP [player value] PlayerCalcEvent)

(defrecord CPRecoverSpeed [player value] PlayerCalcEvent)

(defrecord OverloadRecoverSpeed [player value] PlayerCalcEvent)

(defrecord MaxOverload [player value] PlayerCalcEvent)

(defrecord SkillAttack [player skill target value] PlayerCalcEvent)

(defrecord SkillPerform [player cp overload] AbilityEvent)