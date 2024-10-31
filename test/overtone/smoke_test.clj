(ns overtone.smoke-test
  "Simple tests which exercise overtone and make few assertions.
  Ensures successful complilation of overtone and its examples."
  (:require [overtone.config.log :as log]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.test :as t :refer [deftest is]]
            [overtone.test-helper :refer [eval-in-temp-ns]]))

#_
(doseq [n ((requiring-resolve 'clj-reload.core/find-namespaces) #"overtone\..*examples\..*")]
  (let [testn (symbol (str n "-test"))
        s (namespace-munge testn)
        splits (str/split s #"\.")
        f (doto (apply io/file "test" (concat (butlast splits) [(str (last splits) ".clj")]))
            io/make-parents)]
    (spit f (with-out-str
              ((requiring-resolve 'clojure.pprint/pprint)
               (list 'ns testn
                     (list :require
                           ['clojure.test :refer ['deftest 'is]]
                           [n :as 'sut])))))))
