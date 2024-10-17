(ns overtone.sc.ugen-collide
  "Namespace containing fns to generate UGens, or Unit Generators that
  collide with clojure.core vars:

    * + - / < <= = > >= abs and max min mod not= or
  
  Use overtone.sc.ugen for non-colliding ugens."
  (:refer-clojure :only [ns])
  ;; interns colliding symbols in this ns
  (:require overtone.sc.machinery.ugen.fn-gen))
