(ns overtone.examples.compositions.piano-phase
  (:use overtone.live
        overtone.inst.sampled-piano)
  (:require [overtone.repl.piano-keys :as pk]))

;; Steve Reich's Piano Phase

(defonce piece [:E4 :F#4 :B4 :C#5 :D5 :F#4 :E4 :C#5 :B4 :F#4 :D5 :C#5])

(def active-notes* (atom []))

(defn update-active-notes [player-id t speed n]
  (swap! active-notes* (fn [notes]
                         (let [nplayers (count notes)]
                           (if (<= player-id nplayers)
                             (assoc notes player-id n)
                             (-> notes
                                 ;; pad
                                 (into (repeat (- player-id nplayers 1) nil))
                                 (conj n)))))))

(defn display-notes [player-id t speed n]
  (let [notes (update-active-notes player-id t speed n)
        s (with-out-str
            (run! (fn [p]
                    (println (str "Player " p))
                    (pk/print-piano-keys (subvec notes p (inc p)) {:min-octave 4 :max-octave 5}))
                  (range (count notes))))]
    (locking active-notes*
      (print s)
      (flush))))

(defn player [player-id t speed notes]
  (let [n      (first notes)
        notes  (next notes)
        t-next (+ t speed)]
    (when n
      (at t (sampled-piano (note n)))
      (apply-by t-next #'player [player-id t-next speed notes])
      (display-notes player-id t speed n))))

(def num-notes 1000)


(comment
  (do
    (reset! active-notes* [])
    (player 0 (now) 338 (take num-notes (cycle piece)))
    (player 1 (now) 335 (take num-notes (cycle piece))))
  (do @active-notes*)
  (stop)
  (subvec [1] 0 1)
  )
