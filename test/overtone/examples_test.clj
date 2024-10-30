(ns overtone.examples-test
  (:require [clojure.test :refer [deftest is use-fixtures]]
            [overtone.test-helper :as th]))

(use-fixtures :once th/ensure-server)

(def examples
  '[overtone.examples.sc-book.sc-one])

(deftest examples-test
  (doseq [ns examples]
    (require ns :reload)))
