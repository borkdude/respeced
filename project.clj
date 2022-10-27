(defproject respeced "0.1.3-SNAPSHOT"
  :description "You wrote fdefs. But how are you going to test them?"
  :url "https://github.com/borkdude/respeced"
  :scm {:name "git"
        :url "https://github.com/borkdude/respeced"}
  :license {:name "Eclipse Public License 1.0"
            :url "http://opensource.org/licenses/eclipse-1.0.php"}
  :dependencies [[org.clojure/clojure "1.10.0"]]
  :profiles {:dev {:dependencies
                   [[org.clojure/clojurescript "1.10.439"]
                    [org.clojure/test.check "0.9.0"]]}}
  :deploy-repositories [["clojars" {:url "https://clojars.org/repo"
                                  :username :env/clojars_user
                                  :password :env/clojars_pass
                                  :sign-releases false}]])
