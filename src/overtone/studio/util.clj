(ns overtone.studio.util
  "Util synths"
  {:author "Sam Aaron & Jeff Rose"}
  (:use [overtone.libs.event]
        [overtone.sc.synth]
        [overtone.sc.ugens :exclude [= < <= * min not= > mod - or / >= + abs max and]]))

;; Some utility synths for signal routing and scoping
(defonce __UTIL-SYNTHS__
  (do
    (defsynth control-bus->buf [bus 20 buf 0]
      (record-buf:kr (in:kr bus) buf))

    (defsynth bus->buf [bus 20 buf 0]
      (record-buf (in bus) buf))

    (defsynth bus->bus [in-bus 20 out-bus 0]
      (out out-bus (in in-bus)))))
