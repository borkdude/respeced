(ns respeced.test
  "Macros and function utils for clojure.spec.test.alpha and
  clojure.test. Alpha, subject to change."
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [respeced.impl :as impl])
  #?(:cljs
     (:require-macros
      [respeced.test :refer [with-instrumentation
                             with-unstrumentation
                             caught?
                             check-call
                             check]])))

(impl/deftime

  ;; with-(i/u)nstrumentation avoids using finally as a workaround for
  ;; https://dev.clojure.org/jira/browse/CLJS-2949
  (defmacro with-instrumentation
    "Executes body while instrumenting symbol."
    [symbol & body]
    `(let [was-instrumented?#
           (boolean
            (seq (impl/unstrument ~symbol)))
           ret# (impl/try-return
                 (impl/instrument ~symbol)
                 ~@body)]
       (when-not was-instrumented?#
         (impl/unstrument ~symbol))
       (if (impl/throwable? ret#)
         (throw ret#)
         ret#)))

  (defmacro with-unstrumentation
    "Executes body while unstrumenting symbol."
    [symbol & body]
    `(let [was-instrumented?#
           (boolean
            (seq (impl/unstrument ~symbol)))
           ret# (impl/try-return
                 (impl/unstrument ~symbol)
                 ~@body)]
       (when was-instrumented?#
         (impl/instrument ~symbol))
       (if (impl/throwable? ret#)
         (throw ret#)
         ret#)))

  (defmacro caught?
    "Returns true if body throws spec error for instrumented fn"
    [sym & body]
    `(let [msg#
           (impl/? :clj (try
                          ~@body
                          (catch clojure.lang.ExceptionInfo e#
                            (.getMessage e#)))
                   :cljs (try
                           ~@body
                           (catch js/Error e#
                             (.-message e#))))]
       (clojure.string/starts-with?
        (str msg#)
        (str "Call to " (resolve ~sym)
             " did not conform to spec"))))

  (defmacro check-call
    "Applies args to function resolved by symbol. Checks `:args`, `:ret`
  and `:fn` specs for spec resolved by symbol. Returns return value if
  check succeeded, else throws."
    [symbol args]
    (assert (vector? args))
    `(let [f# (resolve ~symbol)
           spec# (impl/get-spec ~symbol)]
       (impl/check-call* f# spec# ~args)))

  (defmacro check
    "`clojure.spec.test.alpha/check` with third arg for passing
  `clojure.test.check` options."
    ([sym]
     `(check ~sym nil nil))
    ([sym opts]
     `(check ~sym ~opts nil))
    ([sym opts tc-opts]
     `(impl/with-instrument-disabled
        (println "generatively testing" ~sym)
        (let [opts# ~opts
              tc-opts# ~tc-opts
              opts# (update-in opts# [(impl/test-check-kw "opts")]
                               (fn [o#]
                                 (merge o# tc-opts#)))
              ret#
              (impl/test-check ~sym opts#)]
          ret#)))))

(defn successful?
  "Returns `true` if all `clojure.spec.test.alpha/check` tests have
  `pass?` `true.`"
  [stc-result]
  (and (seq stc-result)
       (every? (fn [res]
                 (let [check-ret (get res (impl/test-check-kw "ret"))]
                   (:pass? check-ret)))
               stc-result)))

;;;; Scratch

(comment
  (check `count [nil])
  (check `some [1 1])
  (check `/ [1 1 1 1 1 1])
  (check `/ [0 0])
  )
