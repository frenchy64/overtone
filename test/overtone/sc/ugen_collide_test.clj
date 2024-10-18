(ns overtone.sc.ugen-collide-test
  (:require [clojure.test :refer [deftest is testing]]
            [overtone.sc.ugens :as u :refer [gate:kr]]
            [overtone.sc.machinery.ugen.sc-ugen :as urep]))

; (u/sqrt 10) should call Math/sqrt
; (u/log 10) should call Math/log
; (u/exp 10) should call Math/exp
; (u/floor 10) should call Math/floor
; (u/ceil 10) should call Math/ceil
; (u/sign) could be Math/signum, tho that returns a double instead of int
; (u/log 10) should call (Math/log 10)
; (u/log2 10) ??
; (u/log10 10) should call (Math/log10 10)
; (u/sin 10) should call (Math/sin 10)
; (u/cos 10) should call (Math/cos 10)
; (u/tan 10) should call (Math/tan 10)
; (u/asin 0.5) should call (Math/asin 0.5)
; (u/acos 0.5) should call (Math/acos 0.5)
; (u/atan 0.5) should call (Math/atan 0.5)
; (u/sinh 0.5) should call (Math/sinh 0.5)
; (u/cosh 0.5) should call (Math/cosh 0.5)
; (u/tanh 0.5) should call (Math/tanh 0.5)

;; there used to be ugens called and/or (originally sig-{and,or,xor}).
;; they were renamed to bit-{and,or} and now clojure.core/{and,or} are not shadowed,
;; which this test verifies.
(deftest ugens-colliding-with-macros-test
  (testing "and is not shadowed"
    (is (true? (u/with-overloaded-ugens (and))))
    (is (= 1 (u/with-overloaded-ugens (and 1))))
    (is (= 1 (u/with-overloaded-ugens (and 1 1))))
    (is (= 5 (u/with-overloaded-ugens (and 1 2 3 4 5)))))
  (testing "or is not shadowed"
    (is (nil? (u/with-overloaded-ugens (or))))
    (is (= 1 (u/with-overloaded-ugens (or 1))))
    (is (= 1 (u/with-overloaded-ugens (or 1 1))))
    (is (= 1 (u/with-overloaded-ugens (or 1 nil nil nil nil nil))))
    (is (= 3 (u/with-overloaded-ugens (or nil nil 3 nil nil))))))

(deftest bit-ops-test
  (testing "bit-and"
    (is (thrown? Exception (u/with-overloaded-ugens (bit-and))))
    (is (thrown? Exception (u/with-overloaded-ugens (bit-and :force-ugen))))
    (is (thrown? Exception (u/with-overloaded-ugens (bit-and 1))))
    (is (thrown? Exception (u/with-overloaded-ugens (bit-and 1 :force-ugen))))
    (is (= 1 (u/with-overloaded-ugens (bit-and 1 1))))
    (is (urep/sc-ugen? (u/with-overloaded-ugens (bit-and 1 1 :force-ugen))))
    (is (= 0 (u/with-overloaded-ugens (bit-and 1 2 3 4 5))))
    (is (urep/sc-ugen? (u/with-overloaded-ugens (bit-and 1 2 3 4 5 :force-ugen))))
    (is (urep/sc-ugen? (u/with-overloaded-ugens (bit-and 1 2 (gate:kr) 4 5))))
    (is (urep/sc-ugen? (u/with-overloaded-ugens (bit-and 1 2 (gate:kr) 4 5 :force-ugen)))))
  (testing "bit-or"
    (is (thrown? Exception (u/with-overloaded-ugens (bit-or))))
    (is (thrown? Exception (u/with-overloaded-ugens (bit-or :force-ugen))))
    (is (thrown? Exception (u/with-overloaded-ugens (bit-or 1))))
    (is (thrown? Exception (u/with-overloaded-ugens (bit-or 1 :force-ugen))))
    (is (= 1 (u/with-overloaded-ugens (bit-or 1 1))))
    (is (urep/sc-ugen? (u/with-overloaded-ugens (bit-or 1 1 :force-ugen))))
    (is (= 7 (u/with-overloaded-ugens (bit-or 1 2 3 4 5))))
    (is (urep/sc-ugen? (u/with-overloaded-ugens (bit-or 1 2 (gate:kr) 4 5))))
    (is (urep/sc-ugen? (u/with-overloaded-ugens (bit-or 1 2 (gate:kr) 4 5 :force-ugen))))))
