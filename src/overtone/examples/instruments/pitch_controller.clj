(ns overtone.examples.instruments.pitch-controller
  (:use overtone.core))

;; Fire off this synth and connect an instrument/mic.
;; The pitch will be detected and used to control the saw waves

(defsynth pitch-controlled-saws
  [out-bus 0]
  (let [p   (pitch (sound-in))
        p   (/ p 4)
        p   (lag p 1)]
    (out out-bus (saw [p (+ p (* p 0.01))]))))

(comment
  (require 'overtone.live)
  (pitch-controlled-saws)
  (stop)
  )
