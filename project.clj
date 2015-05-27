(defproject comportex-notebook "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-beta2"]
                 [org.nfrac/comportex "0.0.9-SNAPSHOT"]
                 [com.cognitect/transit-clj "0.8.271"]

                 [org.clojure/clojurescript "0.0-3291"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [comportexviz "0.0.9-SNAPSHOT"]
                 [com.cognitect/transit-cljs "0.8.215"]
                 [reagent "0.5.0"]]

  :source-paths ["src/clj"]
  :resource-paths ["resources" "target/resources"]

  :cljsbuild {:builds [{:source-paths ["src/cljs"]
                        :compiler {:output-dir "target/resources/public"
                                   :output-to "target/resources/public/cnotebook.js"
                                   :source-map "target/resources/public/cnotebook.js.map"
                                   :optimizations :whitespace}}]}

  :profiles {:dev {:dependencies [[org.clojure/clojurescript "0.0-3211"]]
                   :plugins [[lein-cljsbuild "1.0.5"]]}})
