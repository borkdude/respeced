(ns tasks
  (:require [babashka.deps :as deps]
            [babashka.fs :as fs]
            [babashka.process :as p]))

(defn clj-test
  "Runs the clojure tests using the deps DEPS-CLJ-VER-ALIAS (presumambly
  an alias that sets the clojure version to use as dep)."
  [deps-clj-ver-alias]
  (println "=== Running clj-tests" deps-clj-ver-alias)
  (let [alias (format "-A:test%s:clj-tests" deps-clj-ver-alias)]
    (fs/delete-tree "out")
    (-> (deps/clojure [alias])
        p/check)))

(defn cljs-test
  "Runs the clojure tests using the deps DEPS-CLJ-VER-ALIAS (presumambly
  an alias that sets the clojurescript version to use as dep)."
  [deps-clj-ver-alias]
  (let [alias (format "-A:test%s:compile-node-tests" deps-clj-ver-alias)]
    (println "\n=== Compiling node tests" deps-clj-ver-alias)
    (-> (deps/clojure [alias])
        p/check)
    (println "\n=== Running node tests")
    (p/shell "node out/main.js")))

(defn plk-test
  "Runs the clojure tests using the deps DEPS-CLJ-VER-ALIAS (presumambly
  an alias that sets the clojurescript version to use as dep)."
  [deps-clj-ver-alias]
  (println "\n=== Running plk-tests" deps-clj-ver-alias)
  (if (fs/windows?)
    (println "\n=== [SKIPPING] The Planck REPL is not available on MS-Windows.")

    (let [alias (format "-A:test%s:plk-tests" deps-clj-ver-alias)]
      (-> (deps/clojure [alias])
          p/check))))
