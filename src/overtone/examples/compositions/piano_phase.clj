(ns overtone.examples.compositions.piano-phase
  (:use overtone.core
        overtone.inst.sampled-piano))

;; Steve Reich's Piano Phase

(def piece [:E4 :F#4 :B4 :C#5 :D5 :F#4 :E4 :C#5 :B4 :F#4 :D5 :C#5])

(defn player
  [t speed notes]
  (let [n      (first notes)
        notes  (next notes)
        t-next (+ t speed)]
    (when n
      (at t
        (sampled-piano (note n)))
      (apply-by t-next #'player [t-next speed notes]))))

(def num-notes 1000)

(comment
  (require 'overtone.live)
  (do
    (player (now) 338 (take num-notes (cycle piece)))
    (player (now) 335 (take num-notes (cycle piece))))

  (stop))
