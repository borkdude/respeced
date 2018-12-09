(defproject respeced "0.0.1-SNAPSHOT"
  :description "Various tools around clojure.spec.test.alpha"
  :url "https://github.com/borkdude/respeced"
  :scm {:name "git"
        :url "https://github.com/borkdude/respeced"}
  :license {:name "MIT"
            :url "https://mit-license.org/"}
  :dependencies [[org.clojure/clojure "1.10.0-RC4"]]
  :profiles {:dev {:dependencies
                   [[org.clojure/clojurescript "1.10.439"]
                    [org.clojure/test.check "0.9.0"]]}}
  :deploy-repositories [["clojars" {:url "https://clojars.org/repo"
                                  :username :env/clojars_user
                                  :password :env/clojars_pass
                                  :sign-releases false}]])
