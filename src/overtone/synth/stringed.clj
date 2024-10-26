;; A Stringed Synth Generator Macro & Guitar Example Instrument
;;
;; See overtone/examples/instruments/guitar_synth.clj for example usage.
;;
;; Other instruments (like bass-guitar, ukelele, mandolin, etc.) may
;; use the same basic instrument.  Watch this space...
(ns overtone.synth.stringed
  "A Stringed Synth Generator Macro & Guitar Instrument"
  {:author "Roger Allen"}
  (:refer-clojure :exclude [abs])
  (:use [overtone.music pitch time]
        [overtone.sc envelope node server synth ugens]
        [overtone.sc.cgens mix])
  (:require [overtone.sc.ugen-collide :as u]
            [overtone.sc.ugens :as ug]
            [overtone.sc.envelope :as env]))

;; ======================================================================

(defn -gen-stringed-synth
  [{:keys [dur decay coef noise-amp pre-amp amp distort rvb-mix rvb-room rvb-damp lp-freq lp-rq pan out-bus
           free-on-silence note-gate-pairs]}]
  (let [strings (map (fn [[note gate]]
                       (let [frq  (midicps note)
                             nze  (u/* noise-amp (pink-noise))
                             plk  (pluck nze
                                         gate
                                         (u// 1.0 8.0)
                                         (u// 1.0 frq)
                                         decay
                                         coef)]
                         (leak-dc (u/* plk (apply ug/env-gen
                                                  (env/asr 0.0001 1 0.1)
                                                  :gate gate
                                                  (when free-on-silence
                                                    [:action ug/FREE])))
                                  0.995)))
                     note-gate-pairs)
        src (u/* pre-amp (mix strings))
        ;; distortion from fx-distortion2
        k   (u// (u/* 2 distort) (u/- 1 distort))
        dis (u// (u/* src (u/+ 1 k))
                 (u/+ 1 (u/* k (u/abs src))))
        vrb (free-verb dis rvb-mix rvb-room rvb-damp)
        fil (rlpf vrb lp-freq lp-rq)]
    (out out-bus (pan2 (u/* amp fil) pan))))

(def common-synth-params
  '[dur       {:default 10.0  :min 1.0 :max 100.0}
    decay     {:default 30    :min 1   :max 100} ;; pluck decay
    coef      {:default 0.3   :min -1  :max 1}   ;; pluck coef
    noise-amp {:default 0.8   :min 0.0 :max 1.0}
    pre-amp   {:default 6.0   :min 0.0 :max 10.0}
    amp       {:default 1.0   :min 0.0 :max 10.0}
    ;; by default, no distortion, no reverb, no low-pass
    distort   {:default 0.0   :min 0.0 :max 0.9999999999}
    rvb-mix   {:default 0.0   :min 0.0 :max 1.0}
    rvb-room  {:default 0.0   :min 0.0 :max 1.0}
    rvb-damp  {:default 0.0   :min 0.0 :max 1.0}
    lp-freq   {:default 20000 :min 100 :max 20000}
    lp-rq     {:default 1.0   :min 0.1 :max 10.0}
    pan       {:default 0.0   :min -1  :max 1}
    out-bus   {:default 0     :min 0   :max 100}])

(comment
  (def example-map
    (into {} (comp (partition-all 2)
                   (map (fn [[k {:keys [default]}]] [(keyword k) default])))
          common-synth-params))
  ((requiring-resolve 'clojure.pprint/pprint)
   (overtone.sc.machinery.ugen.sc-ugen/simplify-ugen
     (-gen-stringed-synth
       (assoc example-map
              :note-gate-pairs [[3 1] [2 2]]))))
  )

(defmacro gen-stringed-synth
  "Macro to generate a stringed defsynth with distortion, reverb and
   a low-pass filter.  Use the pick-string and strum-strings helper
  functions to play the instrument.

   Note: the strings need to be silenced with a gate -> 0 transition
   before a gate -> 1 transition activates it.  Testing
   showed it needed > 25 ms between these transitions to be effective."
  [name num-strings free-on-silence]
  (let [note-ins (if (= num-strings 1)
                   [(symbol "note")]
                   (apply vector
                          (map #(symbol (format "note-%d" %)) (range num-strings))))
        note-default-ins (apply vector
                                (flatten (map vector
                                              note-ins
                                              (repeat num-strings {:default 60 :min 0 :max 127}))))
        gate-ins (if (= num-strings 1)
                   [(symbol "gate")]
                   (apply vector
                          (map #(symbol (format "gate-%d" %)) (range num-strings))))
        gate-default-ins (apply vector (flatten (map vector
                                                     gate-ins
                                                     (repeat num-strings {:default 0}))))
        both-default-ins (into note-default-ins gate-default-ins)
        note-gate-pairs (apply vector (map vector note-ins gate-ins))
        doc (str "a stringed instrument synth with " num-strings " strings mixed and sent thru\n"
                 "  distortion and reverb effects followed by a low-pass filter.  Use\n"
                 "  the pick-string and strum-strings helper functions to play the\n"
                 "  instrument. Note: the strings need to be silenced with a gate -> 0\n"
                 "  transition before a gate -> 1 transition activates it.\n"
                 (if free-on-silence
                   "\nThis instrument is transient.  When a string becomes silent, it will be freed."
                   "\nThis instrument is persistent.  It will not be freed when the strings go silent."))]
    `(defsynth ~name
       ~doc
       [~@both-default-ins
        ~@common-synth-params]
       (-gen-stringed-synth
         ~(into {:free-on-silence (boolean free-on-silence)
                 :note-gate-pairs note-gate-pairs}
                (comp (partition-all 2)
                      (map (fn [[n]]
                             [(keyword n) n])))
                common-synth-params)))))
;;(macroexpand-1 '(gen-stringed-synth ektara 1 true))

;; ======================================================================
;; common routines for stringed instruments

(defn- fret-to-note
  "given a fret-offset, add to the base note index with special
  handling for -1"
  [base-note offset]
  (if (>= offset 0)
    (+ base-note offset)
    offset))

(defn- mkarg
  "useful for making arguments for the instruments strings"
  [s i]
  (keyword (format "%s-%d" s i)))

(defn- now+
  "Add an epsilon of time to (now) to avoid lots of 'late' error messages"
  []
  (+ (now) 21)) ;; 21ms seems to get rid of most for me.

;; ======================================================================
;; Main helper functions used to play the instrument: pick or strum
(defn pick-string
  "Pick the instrument's string depending on the fret selected.  A
   fret value less than -1 will cause no event; -1 or greater causes
   the previous note to be silenced; 0 or greater will also cause a
   new note event."
  ([the-strings the-inst string-index fret t]
   (let [the-note (fret-to-note (nth the-strings string-index) fret)]
     ;; turn off the previous note
     (if (>= the-note -1)
       (at t (ctl the-inst (mkarg "gate" string-index) 0)))
     ;; NOTE: there needs to be some time between these
     ;; FIXME: +50 seems conservative.  Find minimum.
     (if (>= the-note 0)
       (at (+ t 50) (ctl the-inst
                         (mkarg "note" string-index) the-note
                         (mkarg "gate" string-index) 1)))))
  ([the-chord-frets the-inst string-index fret]
   (pick-string the-chord-frets the-inst string-index fret (now+))))

;; ======================================================================
(defn strum-strings
  "strum a chord on the instrument in a direction (:up or :down) with
   a strum duration of strum-time at t.  If the-chord is a vector, use
   it directly for fret indexes."
  ([chord-fret-map the-strings the-inst the-chord direction strum-time t]
   (let [num-strings (count (chord-fret-map :A))
         ;; ex: [-1 3 2 0 1 0]
         chord-frets (if (vector? the-chord)
                       ;; treat the-chord as a series of frets
                       ;; and gracefully handle odd sized vectors
                       (vec (take num-strings
                                  (into the-chord
                                        (vec (repeat num-strings -1)))))
                       ;; else use the-chord as an index
                       (chord-fret-map the-chord))
         ;; account for unplayed strings for delta time calc. Code
         ;; gets a bit complicated to deal with the case where
         ;; strings are muted and don't count towards the
         ;; strum-time.
         ;; ex: (0 0 1 2 3 4)
         fret-times (map first
                         (rest (reductions
                                #(vector (if (>= (second %1) 0)
                                           (inc (first %1))
                                           (first %1))
                                         %2)
                                [0 -1]
                                chord-frets)))]
     (dotimes [i num-strings]
       (let [j (if (= direction :up) (- num-strings 1 i) i)
             max-t (apply max fret-times)
             dt (if (> max-t 0)
                  (* 1000 (/ strum-time max-t))
                  0)
             fret-delta (if (= direction :up)
                          (- max-t (nth fret-times j))
                          (nth fret-times i))]
         (pick-string the-strings the-inst j
                      (nth chord-frets j)
                      (+ t (* fret-delta dt)))))))
  ([chord-fret-map the-strings the-inst the-chord direction strum-time]
   (strum-strings chord-fret-map the-strings the-inst the-chord
                  direction strum-time (now+)))
  ([chord-fret-map the-strings the-inst the-chord direction]
   (strum-strings chord-fret-map the-strings the-inst the-chord
                  direction 0.05 (now+)))
  ([chord-fret-map the-strings the-inst the-chord]
   (strum-strings chord-fret-map the-strings the-inst the-chord
                  :down 0.05 (now+))))

;; ======================================================================
;; The Guitar Instrument Code
;; ======================================================================
;; A map of chords to frets held for that chord.  This is not all
;; possible guitar chords, just some of them as there are many
;; alternatives to choose from.  Add more as you find/need them.
;;
;; You can pass in your own arrays to strum, too.  The values are the
;; fret number of the string to press.  This selects the note to play.
;;   -1 indicates you mute that string
;;   -2 indicates you leave that string alone & keep the current state
;;      of either playing or not
;;
(def guitar-chord-frets
  {:A    [ -1  0  2  2  2  0 ]
   :A7   [ -1  0  2  0  2  0 ]
   :A9   [  0  0  2  4  2  3 ]
   :Am   [  0  0  2  2  1  0 ]
   :Am7  [  0  0  2  0  1  0 ]

   :Bb   [ -1  1  3  3  3  1 ]
   :Bb7  [ -1 -1  3  3  3  4 ]
   :Bb9  [ -1 -1  0  1  1  1 ]
   :Bbm  [ -1 -1  3  3  2  1 ]
   :Bbm7 [  1  1  3  1  2  1 ]

   :B    [ -1 -1  4  4  4  2 ]
   :B7   [ -1  2  1  2  0  2 ]
   :B9   [  2 -1  1  2  2  2 ]
   :Bm   [ -1 -1  4  4  3  2 ]
   :Bm7  [ -1  2  0  2  0  2 ]

   :C    [ -1  3  2  0  1  0 ]
   :C7   [ -1  3  2  3  1  0 ]
   :C9   [  3  3  2  3  3  3 ]
   :Cm   [  3  3  5  5  4  3 ]
   :Cm7  [  3  3  5  3  4  3 ]

   :Db   [ -1 -1  3  1  2  1 ]
   :Db7  [ -1 -1  3  4  2  4 ]
   :Db9  [  4 -1  3  4  4  4 ]
   :Dbm  [ -1 -1  2  1  2  0 ]
   :Dbm7 [ -1  3  2  1  0  0 ]

   :D    [ -1 -1  0  2  3  2 ]
   :D7   [ -1 -1  0  2  1  2 ]
   :D9   [ -1 -1  4  2  1  0 ]
   :Dm   [ -1  0  0  2  3  1 ]
   :Dm7  [ -1 -1  0  2  1  1 ]

   :Eb   [ -1 -1  5  3  4  3 ]
   :Eb7  [ -1 -1  1  3  2  3 ]
   :Eb9  [ -1 -1  1  0  2  1 ]
   :Ebm  [ -1 -1  4  3  4  2 ]
   :Ebm7 [ -1 -1  1  3  2  2 ]

   :E    [  0  2  2  1  0  0 ]
   :E7   [  0  2  0  1  0  0 ]
   :E9   [  0  2  0  1  3  2 ]
   :Em   [  0  2  2  0  0  0 ]
   :Em7  [  0  2  2  0  3  0 ]

   :F    [  1  3  3  2  1  1 ]
   :F7   [  1 -1  2  2  1 -1 ]
   :F9   [  1  0  3  0  1 -1 ]
   :Fm   [  1  3  3  1  1  1 ]
   :Fm7  [  1  3  3  1  4  1 ]

   :Gb   [  2  4  4  3  2  2 ]
   :Gb7  [ -1 -1  4  3  2  1 ]
   :Gb9  [ -1  4 -1  3  5  4 ]
   :Gbm  [  2  4  4  2  2  2 ]
   :Gbm7 [  2 -1  2  2  2 -1 ]

   :G    [  3  2  0  0  0  3 ]
   :G7   [  3  2  0  0  0  1 ]
   :G9   [ -1 -1  0  2  0  1 ]
   :Gm   [ -1 -1  5  3  3  3 ]
   :Gm7  [ -1  1  3  0  3 -1 ]

   :Ab   [ -1 -1  6  5  4  4 ]
   :Ab7  [ -1 -1  1  1  1  2 ]
   :Ab9  [ -1 -1  1  3  1  2 ]
   :Abm  [ -1 -1  6  4  4  4 ]
   :Abm7 [ -1 -1  4  4  7  4 ]

   :Gadd5 [  3  2  0  0  3  3 ]
   :Cadd9 [ -1  3  2  0  3  3 ]
   :Dsus4 [ -1 -1  0  2  3  3 ]

   })

;; ======================================================================
;; an array of 6 guitar strings: EADGBE
(def guitar-string-notes (map note [:e2 :a2 :d3 :g3 :b3 :e4]))

;; ======================================================================
;; Main helper functions.  Use pick or strum to play the instrument.
(def guitar-pick
  "Pick a single string, takes the instrument, a string number, fret number, and
  timestamp.

  (def g (guitar))
  (guitar-pick g 4 1 (now)) ; play a C, first fret on the B string
  "
  (partial pick-string guitar-string-notes))

(def guitar-strum
  "Strum a given chord, takes the instrument, a chord, strum direction, and time
  between notes.

  (def g (guitar))
  (guitar-strum g :E :down 0.25)
  "
  (partial strum-strings guitar-chord-frets guitar-string-notes))

;; ======================================================================
;; Set's a fret on the-inst

(defn set-fret
  "Sets fret for the-inst on string-index"
  [the-inst string-index fret]
  (let [the-note (fret-to-note (nth guitar-string-notes string-index) fret)]
    (if (= the-note -1) ;mute it
      (ctl the-inst (mkarg "gate" string-index) 0))
    (if (>= the-note 0) ; set other note on string-index
      (ctl the-inst (mkarg "note" string-index) the-note))))

(defn slide-string
  "Slides the-string of the-inst from fret start-fret to fret end-fret.
  Every note in between sounds for duration time. If keep is set the last note
  will be fret end-fret otherwise the string gets muted."
  [the-inst the-string start-fret end-fret start duration keep-note]
  (at start (guitar-pick the-inst the-string start-fret))
  (let [i (atom 1)] ; used to calculate the offset between the sub-slides
    (doseq [fret (if (< end-fret start-fret)
                   (reverse (range end-fret (dec start-fret)))
                   (range start-fret (inc end-fret)))]
      (at (+ (* @i duration) start) (set-fret the-inst the-string fret))
      (if (and (= end-fret fret) (zero? keep-note) )
        (at (+ (* (inc @i)  duration) start) (set-fret the-inst the-string -1)))
      (swap! i inc))))

;; ======================================================================
;; Create the guitar defsynth.  Note that it is persistent and will
;; not be freed when any string goes silent.
(gen-stringed-synth guitar 6 false)

;; ======================================================================
;; Ektara - a single-string synth.  Mainly for use with the midi-poly-player.
;;
;; Since "string" was too generic a name, I asked the google for some
;; help.  Wikipedia tells me that there is a single-stringed
;; instrument called the "Ektara", so that is where the name comes
;; from.
;;
;; For use with midi-poly-player, we need to make the default gate 1.
;; Example:
;;   (def mpp (midi-poly-player (partial ektara :gate 1)))
;;
;; Note that it is transient and will be freed when the string goes
;; silent.
;;
(gen-stringed-synth ektara 1 true)
