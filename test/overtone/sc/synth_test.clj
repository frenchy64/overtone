(ns overtone.sc.synth-test
  (:require [clojure.test :refer [deftest is]]
            [overtone.sc.synth :as sut]
            [overtone.sc.ugens :as ug]
            [overtone.sc.ugen-collide :as u]
            [overtone.sc.envelope :as env]
            [clojure.data :as data]))

(deftest topological-sort-ugens-test
  (let [u (ug/sin-osc 440)]
    (is (= [u] (sut/topological-sort-ugens #{u}))))
  ;(sut/topological-sort-ugens #{(ug/out 0 (u/* [1 2] [3 4]))})
  )
