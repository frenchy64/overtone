(ns overtone.sc.ugen-collide-test
  (:require [clojure.test :refer [deftest is testing]]
            [overtone.sc.ugen-collide :as u]
            [overtone.sc.ugens :refer [gate:kr]]
            [overtone.sc.machinery.ugen.sc-ugen :as urep]))

(deftest colliding-ugens-test
  (testing "="
    (is (= (= 1 5) (u/= 1 5)))
    (is (urep/sc-ugen? (u/= (gate:kr) (gate:kr))))
    ;(is (urep/sc-ugen? (u/= {:a 1 :b 2})))
    ;(is (urep/sc-ugen? (u/= [(gate:kr)] [(gate:kr)])))
    )
  (testing "not="
    (is (= (not= 1 5) (u/not= 1 5)))
    (is (urep/sc-ugen? (u/not= (gate:kr) (gate:kr))))
    ;(is (urep/sc-ugen? (u/not= {:a 1 :b 2})))
    ;(is (urep/sc-ugen? (u/not= [(gate:kr)] [(gate:kr)])))
    )
  (testing "and"
    (is (urep/sc-ugen? (u/and 1 1)))
    (is (urep/sc-ugen? (u/and 1 1)))
    (is (urep/sc-ugen? (u/and 1 2 3 4 5)))
    )
  (testing "or"
    (is (urep/sc-ugen? (u/or 1 1)))
    (is (urep/sc-ugen? (u/or 1 2 3 4 5)))
    )
  (doseq [op '[abs max min mod not=]]
    (testing (pr-str op)
      (let [uop (ns-resolve 'overtone.sc.ugen-collide op)
            cop (ns-resolve 'clojure.core op)]
        (is (= (cop 1) (uop 1)))
        (is (urep/sc-ugen? (uop [1])))
        ))
    )
  (doseq [op '[* + - / < <= > >= max min mod]]
    (testing (pr-str op)
      (let [uop (ns-resolve 'overtone.sc.ugen-collide op)
            cop (ns-resolve 'clojure.core op)]
        (is (= (cop 1 5) (uop 1 5)))
        (is (urep/sc-ugen? (uop [1] 5)))
        (is (urep/sc-ugen? (uop (gate:kr) 5))))))
)

(overtone.sc.ugens/with-overloaded-ugens
  (and (gate:kr) (gate:kr)))
;=> (clojure.core/let [and__5598__auto__ 3] (if and__5598__auto__ (clojure.core/and 4 5) and__5598__auto__))

(overtone.sc.ugens/with-overloaded-ugens
  (or 1 2 3 4 5))
;=> (clojure.core/let [or__5600__auto__ 3] (if or__5600__auto__ or__5600__auto__ (clojure.core/or 4 5)))
