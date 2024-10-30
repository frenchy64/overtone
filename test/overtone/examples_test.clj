(ns overtone.examples-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clj-reload.core :as reload]
            [overtone.test-helper :as th]))

(use-fixtures :once th/ensure-server)

(def examples
  (-> (reload/find-namespaces #"overtone\..*examples\..*")
      (disj
        'overtone.examples.compositions.at-all
        'overtone.examples.compositions.auto-dubstep
        'overtone.examples.compositions.auto-dubstep-bass
        'overtone.examples.compositions.clapping-music
        'overtone.examples.compositions.euclidean-rhythms
        'overtone.examples.compositions.extemp-piano
        'overtone.examples.compositions.funk
        'overtone.examples.compositions.jazz
        'overtone.examples.compositions.piano-phase
        'overtone.examples.compositions.rotater
        'overtone.examples.getting-started.basic
        'overtone.examples.getting-started.intro
        'overtone.examples.getting-started.melody
        'overtone.examples.getting-started.pragpub-article
        'overtone.examples.getting-started.rhythm
        'overtone.examples.getting-started.tutorial
        'overtone.examples.getting-started.video
        'overtone.examples.instruments.dubstep
        'overtone.examples.instruments.external
        'overtone.examples.instruments.guitar-synth
        'overtone.examples.instruments.monotron
        'overtone.examples.instruments.pitch-controller
        'overtone.examples.instruments.slide-guitar
        'overtone.examples.instruments.space
        'overtone.examples.instruments.thx
        'overtone.examples.instruments.vocoder
        'overtone.examples.midi.basic
        'overtone.examples.midi.keyboard
        'overtone.examples.monome.monomestep
        'overtone.examples.monome.sample-looper
        'overtone.examples.monome.satie
        'overtone.examples.notation.mad
        'overtone.examples.notation.mad2
        'overtone.examples.synthesis.cgen-example
        'overtone.examples.synthesis.deci-wobble
        'overtone.examples.synthesis.demand
        'overtone.examples.synthesis.feedback
        'overtone.examples.synthesis.filters
        'overtone.examples.synthesis.fm
        'overtone.examples.synthesis.fourier
        'overtone.examples.synthesis.fx
        'overtone.examples.synthesis.kick
        'overtone.examples.synthesis.local-buffer
        'overtone.examples.synthesis.poll
        'overtone.examples.synthesis.samples
        'overtone.examples.timing.internal-metro
        'overtone.examples.timing.internal-sequencer
        'overtone.examples.timing.one-bar-sequencer
        'overtone.examples.ugens.blowpass4
        'overtone.examples.ugens.wobble
        'overtone.examples.workshops.resonate2013.ex01_phrasestudy
        'overtone.examples.workshops.resonate2013.ex02_bday
        'overtone.examples.workshops.resonate2013.ex03_graph
        'overtone.examples.workshops.resonate2013.ex04_midi
        'overtone.examples.workshops.resonate2013.ex05_synthesis
        'overtone.examples.workshops.resonate2013.ex06_quilstep
        'overtone.sc.examples.audio-in
        'overtone.sc.examples.blackrain
        'overtone.sc.examples.compander
        'overtone.sc.examples.demand
        'overtone.sc.examples.dyn
        'overtone.sc.examples.membrane
        'overtone.sc.examples.osc
        'overtone.sc.examples.trig
        'overtone.sc.examples.vosim)))

(deftest examples-test
  (doseq [ns examples]
    (testing (pr-str ns)
      (is (nil? (require ns :reload))))))
