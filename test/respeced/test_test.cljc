(ns respeced.test-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.test.alpha :as stest]
   [clojure.test :as t :refer [deftest is testing]]
   [respeced.impl :as impl]
   [respeced.test :refer [with-instrumentation
                          with-unstrumentation
                          caught?
                          check-call
                          check
                          successful?]]))

(defn foo [n]
  "ret")

(deftest check-call-test
  (s/fdef foo
    :args (s/cat :n number?)
    :ret number?)

  (is (thrown-with-msg?
       #?(:clj clojure.lang.ExceptionInfo
          :cljs ExceptionInfo)
       #"Specification-based check failed"
       (check-call `foo [1])))

  (s/fdef foo
    :args (s/cat :n number?)
    :ret string?)

  (is (= "ret" (check-call `foo [1]))))

(deftest instrument-test
  (s/fdef foo
    :args (s/cat :n number?)
    :ret string?)

  (testing "manual instrument"
    (stest/instrument `foo)
    (is (caught? `foo (foo "not a number"))))

  (testing "no instrumentation"
    (with-unstrumentation `foo
      (is (= "ret" (foo "not a number")))))

  (testing "manual instrumentation is restored"
    (is (caught? `foo (foo "not a number"))))

  (testing "undo manual instrumentation"
    (stest/unstrument `foo)
    (is (= "ret" (foo "not a number"))))

  (testing "with instrumentation"
    (with-instrumentation `foo
      (is (caught? `foo (foo "not a number")))))

  (testing "no instrumentation"
    (is (= "ret" (foo "not a number")))))

(deftest check-test
  (s/fdef foo
    :args (s/cat :n number?)
    :ret string?)
  (testing "successful?"
    (is (not (successful? [])))
    (is (successful? [{(impl/test-check-kw "ret") {:pass? true}}]))
    (is (not (successful? [{(impl/test-check-kw "ret") {:pass? false}}]))))
  (testing "check"
    (let [ret (check `foo nil {:num-tests 42})
          rets (map (impl/test-check-kw "ret") ret)]
      (is (successful? ret))
      (is (every? #(= 42 (:num-tests %)) rets)))))

;;;; Scratch

(comment
  (t/run-tests)
  )
