(ns overtone.sc.synth-test
  (:require [clojure.test :refer [deftest is]]
            [overtone.sc.synth :as sut]
            [overtone.sc.ugens :as ug]))

(deftest bad-synth-params-test
  (is (thrown-with-msg? Exception #"Param must be unqualified symbol: overtone.sc.synth-test/freq"
                        (try (eval `(sut/defsynth foo [freq 440 freq 550] (ug/out 0 (ug/rlpf (ug/saw [220 663]) (ug/x-line:kr 20000 2 1 ug/FREE)))))
                             (catch Exception e (throw (.getCause e))))))
  (is (thrown? Exception #"Repeated synth param name: freq"
               (try (eval `(sut/defsynth foo [~'freq 440 ~'freq 550] (ug/out 0 (ug/rlpf (ug/saw [220 663]) (ug/x-line:kr 20000 2 1 ug/FREE)))))
                    (catch Exception e (throw (.getCause e)))))))
