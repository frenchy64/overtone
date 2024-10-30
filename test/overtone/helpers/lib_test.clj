(ns overtone.helpers.lib-test
  (:require [clojure.test :refer [deftest is]]
            [overtone.helpers.lib :as sut]))

(deftest arg-mapper-test
  (is (= {:freq 440.0} (sut/arg-mapper '() '(:freq) {:freq 440.0})))
  (is (= {:freq 550} (sut/arg-mapper '(550) '(:freq) {:freq 440.0})))
  (is (= {:freq 660} (sut/arg-mapper '(660) '(:freq) {:freq 440.0})))
  (is (= {:freq 880} (sut/arg-mapper '({:freq 880}) '(:freq) {:freq 440.0}))))
