(ns overtone.sc.synth-test
  (:require [clojure.test :refer [deftest is]]
            [overtone.sc.synth :as sut]
            [overtone.sc.ugens :as ug]))

(deftest topological-sort-ugens-test
  (is (= ::FIXME (sut/topological-sort-ugens (ug/sin-osc 440))))
  )
