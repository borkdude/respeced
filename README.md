# respeced
[![CircleCI](https://circleci.com/gh/borkdude/respeced/tree/master.svg?style=svg)](https://circleci.com/gh/borkdude/respeced/tree/master)
[![Clojars Project](https://img.shields.io/clojars/v/respeced.svg)](https://clojars.org/respeced)

## Rationale

This library provides various tools around `clojure.spec.test.alpha`.

## Example usage

``` clojure
$ clj -Sdeps '{:deps {org.clojure/test.check {:mvn/version "RELEASE"}}}'
Clojure 1.10.0-beta5

user=> (require '[respeced.test :as test])
nil

user=> (require '[clojure.spec.alpha :as s])
nil

user=> (s/fdef foo :args (s/cat :n number?) :ret number?)
user/foo

;; this function has the wrong return value according to the spec:

user=> (defn foo [n] "ret")
#'user/foo

;; test/check-call helps with checking `:ret` and `:fn` specs:

user=> (test/check-call `foo [1])
Execution error - invalid arguments to respeced.test$do_check_call/invokeStatic at (test.cljc:138).
"ret" - failed: number? at: [:ret]

;; change the spec:

user=> (s/fdef foo :args (s/cat :n number?) :ret string?)
user/foo

;; no error anymore:

user=> (test/check-call `foo [1])
"ret"

;; instrument a function within a scope:

user=> (test/with-instrumentation `foo (foo "a"))
Execution error - invalid arguments to user/foo at (REPL:1).
"a" - failed: number? at: [:n]

;; not instrumented:

user=> (foo "a")
"ret"

;; `test/check` has a third arg for passing `clojure.test.check` options:

user=> (test/check `foo nil {:num-tests 1})
generatively testing user/foo
({:spec #object[clojure.spec.alpha$fspec_impl$reify__2524 0x72bd06ca "clojure.spec.alpha$fspec_impl$reify__2524@72bd06ca"], :clojure.spec.test.check/ret {:result true, :pass? true, :num-tests 1, :time-elapsed-ms 1, :seed 1541249961647}, :sym user/foo})

;; validate if generative test was successful:

user=> (test/successful? *1)
true

user=>
```

## Tests

### Clojure

    clj -A:test:clj-tests
     
### ClojureScript

    script/cljs-tests
    
### Self-Hosted ClojureScript
   
    plk -A:test:plk-tests

## License

Copyright © 2018 Michiel Borkent

Distributed under the MIT License. See LICENSE.
