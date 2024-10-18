(ns overtone.sc.ugen-collide-test
  (:require [clojure.test :refer [deftest is testing]]
            [overtone.sc.ugen-collide :as u]
            [overtone.sc.ugens :refer [gate:kr]]
            [overtone.sc.machinery.ugen.sc-ugen :as urep]))

;; TODO docstrings for colliding functions talk about single
;; arg maps but none of them support it
(deftest colliding-ugens-test
  (testing "="
    (is (= (= 1) (u/= 1)))
    (is (= (= 1 5) (u/= 1 5)))
    (is (urep/sc-ugen? (u/= (gate:kr) (gate:kr))))
    ;(is (urep/sc-ugen? (u/= {:a 1 :b 2})))
    ;(is (urep/sc-ugen? (u/= [(gate:kr)] [(gate:kr)])))
    )
  (testing "not="
    (is (= (not= 1 5) (u/not= 1 5)))
    ;(is (= (not= {}) (u/not= {})))
    (is (urep/sc-ugen? (u/not= (gate:kr) (gate:kr))))
    ;(is (urep/sc-ugen? (u/not= {:a 1 :b 2})))
    ;(is (urep/sc-ugen? (u/not= [(gate:kr)] [(gate:kr)])))
    )
  (testing "and"
    (is (urep/sc-ugen? (u/and 1 1)))
    (is (urep/sc-ugen? (u/and 1 1)))
    (is (urep/sc-ugen? (u/and 1 2 3 4 5))))
  (testing "or"
    (is (urep/sc-ugen? (u/or 1 1)))
    (is (urep/sc-ugen? (u/or 1 2 3 4 5))))
  (testing "abs"
    (is (= (abs 1) (u/abs 1)))
    (is (urep/sc-ugen? (u/abs [1]))))
  ;; foldable binary ugens
  (doseq [op '[* + - / < <= > >= max min mod]]
    (testing (pr-str op)
      (let [uop (ns-resolve 'overtone.sc.ugen-collide op)
            cop (ns-resolve 'clojure.core op)]
        (is (= (cop 1 5) (uop 1 5)))
        (is (= (cop 5 1) (uop 5 1)))
        (is (urep/sc-ugen? (uop [1] 5)))
        (is (urep/sc-ugen? (uop (gate:kr) 5)))))))
