(ns overtone.live
  (:refer-clojure :exclude [abs])
  (:require [overtone.api]))

(overtone.api/immigrate-overtone-api)

(if *compile-files*
  (println "--> (use 'overtone.live :reload) or restart JVM to use SuperCollider after compilation")
  (defonce __AUTO-BOOT__
    (boot-server-and-mixer-if-disconnected)))
