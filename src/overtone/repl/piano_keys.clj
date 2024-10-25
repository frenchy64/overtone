(ns overtone.repl.piano-keys
  (:require [clojure.string :as str]
            [overtone.music.pitch :as p]))

;; https://asciiart.website/index.php?art=music/pianos
;; by Alexander Craxton
(def ^:private piano-ascii-template
  #_
  (str/join "\n"
            ["_HHH________________________"
             "│  █C█ █D█  │  █F█ █G█ █A█  │"
             "│_c_│_d_│_e_│_f_│_g_│_a_│_b_│"])
  #_
  (str/join "\n"
            ["HH             "
             "┃ █ █ ┃ █ █ █ ┃"
             "┃ C D ┃ F G A ┃"
             "┃c┃d┃e┃f┃g┃a┃b┃"])
  (str/join "\n"
            ["┃H█ █ ┃ █ █ █ ┃"
             "┃ C D ┃ F G A ┃"
             "┃c┃d┃e┃f┃g┃a┃b┃"]))

;; TODO override min/max octaves
;; TODO intermediate data representation
(defn print-piano-keys [notes]
  (let [notes (into #{} (map p/note) notes)
        octaves (into (sorted-map)
                      (group-by (comp :octave p/note-info p/find-note-name) notes))
        inst (fn [octave template notes]
               (-> template
                   (str/escape
                     (into {} (map (fn [n]
                                     (let [play? (some #(= n (mod % 12)) notes)
                                           idx (mod n 12)
                                           big-key? (#{0 2 4 5 7 9 11} n)
                                           on "●"
                                           off (if big-key?
                                                 \_
                                                 "█")
                                           v (if play? on off)]
                                       (case idx
                                         0 {\c  v}
                                         1 {\C  v}
                                         2 {\d  v}
                                         3 {\D  v}
                                         4 {\e  v}
                                         5 {\f  v}
                                         6 {\F  v}
                                         7 {\g  v}
                                         8 {\G  v}
                                         9 {\a  v}
                                         10 {\A v}
                                         11 {\b v}))))
                           (range 12)))
                   (str/replace "H" (if (= -1 octave)
                                      "-"
                                      (str octave)))))
        start-octave (some-> octaves first key)
        end-octave (some-> octaves last key)
        has-middle-octaves? (< 2 (count octaves))]
    (println
      (if (= start-octave end-octave)
        (inst (or start-octave 4) piano-ascii-template notes)
        (let [roll-at (fn [octave]
                        (let [pos (if (= start-octave octave)
                                    :start
                                    (if (= end-octave octave)
                                      :end
                                      :middle))]
                          (->> piano-ascii-template
                               str/split-lines
                               ;; always trim the left side
                               (mapv #(subs % 0
                                            ((case pos
                                               :start dec
                                               :middle dec
                                               :end identity)
                                             (count %))))
                               (str/join "\n"))))
              join-rolls (fn [rolls]
                           (str/join "\n"
                                     (apply mapv (fn [& lines]
                                                   (apply str lines))
                                            (mapv str/split-lines rolls))))
              rolls (into [] (map (fn [[octave notes]]
                                    (inst octave (roll-at octave) notes)))
                          octaves)]
          (join-rolls rolls))))))

(comment
  (print-piano-keys [60 64 67])
  (print-piano-keys [:C4 :E4 :G4 :C5])
  (print-piano-keys [:E4 :G4 :C5])
  (print-piano-keys [:C4 :G4 :C5 :E5 :G5 :C6])
  (print-piano-keys (p/rand-chord :c4 :diminished 3 12))
(print-piano-keys (p/rand-chord :c-1 :9sus4 12 80))
;=┃-█ █ ┃ █ █ █ ┃0█ █ ┃ █ █ █ ┃1█ █ ┃ █ █ █ ┃2█ █ ┃ █ █ █ ┃3█ █ ┃ █ █ █ ┃4█ █ ┃ █ █ █ ┃5█ █ ┃ █ █ █ ┃
;=┃ █ █ ┃ █ █ ● ┃ █ █ ┃ █ █ █ ┃ █ █ ┃ █ █ ● ┃ █ █ ┃ █ █ ● ┃ █ █ ┃ █ █ ● ┃ █ █ ┃ █ █ █ ┃ █ █ ┃ █ █ █ ┃
;=┃_┃_┃_┃_┃_┃_┃_┃_┃_┃_┃_┃●┃_┃_┃_┃_┃_┃_┃_┃_┃_┃_┃_┃_┃_┃_┃_┃_┃●┃●┃_┃●┃●┃_┃_┃●┃_┃_┃_┃●┃_┃_┃_┃_┃_┃●┃_┃_┃_┃
nil

  (print-piano-keys [:C4 :G4 :C5 :E5 :G5 :C6])
  (print-piano-keys [:C4 :E4 :G4 :B4 :D5])
  (print-piano-keys [60 64 70])
  (print-piano-keys [61])
  )
