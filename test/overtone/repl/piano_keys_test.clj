(ns overtone.repl.piano-keys-test
  (:require [overtone.repl.piano-keys :as sut]
            [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]))

(deftest piano-keys-test
  (is (= [{:octave 4, :key-states [:off :off :off :off :off :off :off :off :off :off :off :off], :style :start+end}]
         (sut/piano-keys [])))
  (is (= [{:octave -1, :key-states [:on :off :off :off :off :off :off :off :off :off :off :off], :style :start+end}]
         (sut/piano-keys [0])))
  (is (= [{:octave 4, :key-states [:on :off :off :off :off :off :off :off :off :off :off :off], :style :start+end}]
         (sut/piano-keys [60])))
  (is (= [{:octave 4, :key-states [:on :off :off :off :off :off :off :off :off :off :off :off], :style :start}
          {:octave 5, :key-states [:on :off :off :off :off :off :off :off :off :off :off :off], :style :end}]
         (sut/piano-keys [60 72])))
  (is (= [{:octave 4, :key-states [:on :off :off :off :off :off :off :on :off :off :off :off], :style :start}
          {:octave 5, :key-states [:on :off :off :off :on :off :off :on :off :off :off :off], :style :middle}
          {:octave 6, :key-states [:on :off :off :off :off :off :off :off :off :off :off :off], :style :end}]
         (sut/piano-keys [:C4 :G4 :C5 :E5 :G5 :C6]))))

(deftest print-piano-keys-test
  (is (= (str/join "\n" ["┃4█ █ ┃ █ █ █ ┃5█ █ ┃ █ █ █ ┃"
                         "┃ █ █ ┃ █ █ █ ┃ █ █ ┃ █ █ █ ┃"
                         "┃●┃_┃●┃_┃●┃_┃_┃●┃_┃_┃_┃_┃_┃_┃"])
         (str/trimr (with-out-str (sut/print-piano-keys [:C4 :E4 :G4 :C5])))))
  (is (= (str/join "\n" ["┃4█ █ ┃ █ █ █ ┃"
                         "┃ █ █ ┃ █ █ █ ┃"
                         "┃_┃_┃_┃_┃_┃_┃_┃"])
         (str/trimr (with-out-str (sut/print-piano-keys [])))))
  )
