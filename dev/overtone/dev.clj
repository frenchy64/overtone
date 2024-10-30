(ns overtone.dev
  (:require [clj-reload.core :as reload]))

(reload/init
  {:dirs ["src" "dev" "test"]
   :no-reload '[overtone.examples.workshops.resonate2013.ex03_graph
                overtone.libs.app-icon
                overtone.examples.getting-started.pragpub-article
                overtone.examples.monome.monomestep
                overtone.examples.monome.sample-looper
                overtone.examples.synthesis.samples
                overtone.examples.workshops.resonate2013.ex06_quilstep
                overtone.examples.midi.basic
                overtone.examples.monome.satie]})

(defn reload-code [])
(defn reload-server []
  (when-some [f (resolve 'overtone.sc.server/kill-server)]
    (f))
  (reload/reload
    {:only :all}))
