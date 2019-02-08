(ns ^:no-doc respeced.impl
  (:require
   #?(:clj [clojure.spec-alpha2 :as s]
      :cljs [clojure.spec.alpha :as s])
   #?(:clj [clojure.spec-alpha2.test :as stest]
      :cljs [clojure.spec.test.alpha :as stest])
   #?(:cljs [goog.string])
   [clojure.test.check]
   [clojure.test.check.properties])
  #?(:cljs
     (:require-macros
      [respeced.impl :refer [deftime ?
                             with-instrument-disabled
                             instrument
                             unstrument
                             get-spec
                             test-check
                             try-return]])))

(defmacro deftime
  "Private. deftime macro from https://github.com/cgrand/macrovich"
  [& body]
  (when #?(:clj (not (:ns &env))
           :cljs (when-let [n (and *ns* (ns-name *ns*))]
                   (re-matches #".*\$macros" (name n))))
    `(do ~@body)))

(deftime
  
  (defmacro ?
    "Private. case macro from https://github.com/cgrand/macrovich"
    [& {:keys [cljs clj]}]
    (if (contains? &env '&env)
      `(if (:ns ~'&env) ~cljs ~clj)
      (if #?(:clj (:ns &env) :cljs true)
        cljs
        clj)))

  ;; aliases so you don't have to require spec as clojure.spec.test.alpha in cljs
  ;; before using this namespace, see #95
  (defmacro with-instrument-disabled
    "Private."
    [& body]
    `(? :clj
        (clojure.spec-alpha2.test/with-instrument-disabled ~@body)
        :cljs
        (cljs.spec.test.alpha/with-instrument-disabled ~@body)))

  (defmacro instrument
    "Private."
    [symbol]
    `(? :clj
        (clojure.spec-alpha2.test/instrument ~symbol)
        :cljs
        (cljs.spec.test.alpha/instrument ~symbol)))

  (defmacro unstrument
    "Private."
    [symbol]
    `(? :clj
        (clojure.spec-alpha2.test/unstrument ~symbol)
        :cljs
        (cljs.spec.test.alpha/unstrument ~symbol)))

  (defmacro get-spec
    "Private."
    [symbol]
    `(? :clj
        (clojure.spec-alpha2/get-spec ~symbol)
        :cljs
        (cljs.spec.alpha/get-spec ~symbol)))

  (defmacro test-check
    "Private."
    [symbol opts]
    `(? :clj
        (clojure.spec-alpha2.test/check ~symbol ~opts)
        :cljs
        (cljs.spec.test.alpha/check ~symbol ~opts))))

(defn throwable?
  "Private."
  [e]
  (instance? #?(:clj Throwable
                :cljs js/Error) e))

(deftime
  
  (defmacro try-return
    "Private. Executes body and returns exception as value"
    [& body]
    `(try ~@body
          (catch ~(? :clj 'Exception :cljs ':default) e#
            e#))))

#_(defn- check-call
  "Returns true if call passes specs, otherwise *returns* an exception
with explain-data + ::s/failure."
  [f specs args]
  (let [cargs (when (:args specs) (s/conform (:args specs) args))]
    (if (= cargs ::s/invalid)
      (explain-check args (:args specs) args :args)
      (let [ret (apply f args)
            cret (when (:ret specs) (s/conform (:ret specs) ret))]
        (if (= cret ::s/invalid)
          (explain-check args (:ret specs) ret :ret)
          (if (and (:args specs) (:ret specs) (:fn specs))
            (if (s/valid? (:fn specs) {:args cargs :ret cret})
              true
              (explain-check args (:fn specs) {:args cargs :ret cret} :fn))
            true))))))

(def ^:private explain-check
  #?(:clj #'clojure.spec-alpha2.test/explain-check
     :cljs #'clojure.spec.test.alpha/explain-check))

(def ^:private valid?
  #?(:clj #'clojure.spec-alpha2/valid?
     :cljs #'clojure.spec.alpha/valid?))

(defn do-check-call
  "Private. From clojure.spec.test.alpha, adapted for respeced"
  [f specs args]
  (with-instrument-disabled
    (let [cargs (when (:args specs) (s/conform (:args specs) args))]
      (if (= cargs ::s/invalid)
        (explain-check args (:args specs) args :args)
        (let [ret (apply f args)
              cret (when (:ret specs) (s/conform (:ret specs) ret))]
          (if (= cret ::s/invalid)
            (explain-check args (:ret specs) ret :ret)
            (if (and (:args specs) (:ret specs) (:fn specs))
              (if (valid? (:fn specs) {:args cargs :ret cret})
                ret
                (explain-check args (:fn specs) {:args cargs :ret cret} :fn))
              ret)))))))

(defn check-call*
  "Private."
  [f spec args]
  (let [ret (do-check-call f spec args)
        ex? (throwable? ret)]
    (if ex?
      (throw ret)
      ret)))

(defn test-check-kw
  "Private. Returns qualified keyword used for interfacing with
  clojure.test.check"
  [name]
  (keyword #?(:clj "clojure.spec.test.check"
              :cljs
              (if (and *clojurescript-version*
                       (pos? (goog.string/compareVersions "1.10.539"
                                                          *clojurescript-version*)))
                "clojure.test.check"
                "clojure.spec.test.check")) name))
