(ns overtone.examples.synthesis.local-buffer
  (:use overtone.live))

;;original sc lang:
;;(
;;{
;;var z = Decay.ar(Dust.ar(1.dup, 0.1), 0.3, WhiteNoise.ar);
;;BufCombC.ar(LocalBuf(SampleRate.ir, 2), z, XLine.kr(0.0001, 0.01, 20), 0.2);
;;}.play
;;)

(defsynth blips []
  (let [z (decay:ar (dust:ar [1 1] 0.1) 0.3 (white-noise:ar))]
    (out 0 (buf-comb-c:ar (local-buf (sample-rate) 2) z (x-line:kr 0.0001 0.01 20) 0.2))))

;(blips)
;(stop)
