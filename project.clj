(defproject clojure-rest "0.1.0-SNAPSHOT"
  :description "REST service for game server API"
  :url "http://github.com/humorless/gameapi_example/"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [org.clojure/tools.logging "0.4.0"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [org.clojure/java.jdbc "0.7.0-beta5"]
                 [org.xerial/sqlite-jdbc "3.19.3"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:init clojure-rest.database/init
         :destroy clojure-rest.database/destroy
         :handler clojure-rest.core/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
