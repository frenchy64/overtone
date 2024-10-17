(ns overtone.helpers.ns)

(defn immigrate
  "Create a public var in this namespace for each public var in the
  namespaces named by ns-names. The created vars have the same name, value
  and metadata as the original except that their :ns metadata value is this
  namespace. If ns is a map, should have a :ns key naming the namespace.
  :exclude can be a set of symbols naming vars to not immigrate."
  [& ns-names]
  (doseq [ns ns-names
          :let [{:keys [ns exclude]} (if (symbol? ns)
                                       {:ns ns}
                                       ns)]]
    (doseq [[sym ^clojure.lang.Var var] (ns-publics ns)
            :when (not (contains? exclude sym))]
      (let [sym (with-meta sym (assoc (meta var) :orig-ns ns))]
        (if (.isBound var)
          (intern *ns* sym (if (fn? (var-get var))
                             var
                             (var-get var)))
          (intern *ns* sym))))))
