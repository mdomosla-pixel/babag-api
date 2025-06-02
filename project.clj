(defproject babag-api "1.1.1-SNAPSHOT"
  :description "babag SMS Gateway - HTTP API"
  :url "https://github.com/karolisl/babag-api"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.395"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/tools.nrepl "0.2.12"]
                 [org.clojure/data.codec "0.1.0"]
                 [org.slf4j/slf4j-log4j12 "1.7.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jmdk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [compojure "1.5.1"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [mount "0.1.10"]
                 [metosin/compojure-api "1.1.9"]
                 [com.novemberain/langohr "4.0.0"]
                 [danlentz/clj-uuid "0.1.6"]
                 [cheshire "5.6.3"]
                 [cprop "0.1.10"]
                 [less-awful-ssl "1.0.1"]
                 [listora/again "0.1.0"]
                 [clj_manifest "0.2.0"]
                 [clj-statsd "0.4.0"]
                 [clj-time "0.13.0"]
                 [org.clojure/data.codec "0.1.0"]
                 [digest "1.4.5"]
                 [hikari-cp "1.7.6"]
                 [com.impossibl.pgjdbc-ng/pgjdbc-ng "0.7.1"]
                 [org.clojure/java.jdbc "0.7.0"]]
  :plugins [[lein-ring "0.9.7"]
            [stackoverflow/lein-jdeb "1.0.5"]
            [lein-kibit "0.1.2"]
            [lein-shell "0.5.0"]
            [lein-auto "0.1.3"]]
  :source-paths ["src" "checkouts/babag-common/src"]
  :ring {:handler babag.api.handler/app}
  :profiles
  {:dev {:source-paths ["src" "dev"]
         :jvm-opts     ["-Dconf=resources/dev-config.edn"]
         :dependencies [[javax.servlet/servlet-api "2.5"]
                        [org.clojure/tools.namespace "0.2.11"]
                        [circleci/bond "0.2.9"]
                        [ring/ring-mock "0.3.0"]]}}
  :main babag.api.main
  :aot [babag.api.main]
  :manifest {"Version"  ~#(:version %)
             "Build-Id" ~(fn [& _] (or (System/getenv "BUILD_ID") "NON-CI-BUILD"))}
  :uberjar {:main babag.api.main
            :aot  :all}
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["shell" "git" "commit" "-S" "-a" "-m" "Version ${:version}"]
                  ["vcs" "tag"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["shell" "git" "commit" "-S" "-a" "-m" "Version ${:version}"]
                  ["vcs" "push"]]
  :jdeb {
         :deb-control-dir "src/deb/control"
         :data-set        [{:src      "target/"
                            :dst      "babag-api.jar"
                            :type     :directory
                            :includes #{"*-standalone.jar"}
                            :mapper   {:type   :perm
                                       :prefix "/opt/babag-api/lib"}}
                           {:src      "src/deb/bin/babag-api"
                            :type     :file
                            :conffile true
                            :mapper   {:type     :perm
                                       :prefix   "/opt/babag-api/bin"
                                       :filemode "755"}}
                           {:src      "src/deb/default"
                            :type     :directory
                            :conffile true
                            :mapper   {:type   :perm
                                       :prefix "/etc/default"}}
                           {:src    "src/deb/systemd"
                            :type   :directory
                            :mapper {:type   :perm
                                     :prefix "/lib/systemd/system"}}
                           {:paths  ["/var/log/babag" "/var/run/babag"]
                            :type   :template
                            :mapper {:type  :perm
                                     :user  "root"
                                     :group "root"}}]})

