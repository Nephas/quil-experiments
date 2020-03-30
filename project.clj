(defproject gravsim_clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.520"]

                 ;Server
                 [compojure "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [environ "1.0.0"]

                 ;Game
                 [quil "2.7.1"]]

  :repl-options {:init-ns gravsim.core}
  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-figwheel "0.5.15"]
            [environ/environ.lein "0.3.1"]]

  ;===== GAME =====;
  :main gravsim.core
  :aot [gravsim.core]
  :uberjar-name "gravsim-standalone.jar"

  ;===== WEBAPP =====;
  :hooks [leiningen.cljsbuild]
  :clean-targets ^{:protect false} ["resources/src/gravsim.js"]
  :cljsbuild {:builds [{:id           "dummy"
                        :source-paths ["src"]
                        :compiler     {:main          "dummy.core"
                                       :output-to     "resources/src/dummy.js"
                                       :output-dir    "resources/optimized/dummy"
                                       :asset-path    "optimized/dummy"
                                       :optimizations :advanced}}
                       {:id           "conway"
                        :source-paths ["src"]
                        :compiler     {:main          "conway.core"
                                       :output-to     "resources/src/conway.js"
                                       :output-dir    "resources/optimized/conway"
                                       :asset-path    "optimized/conway"
                                       :optimizations :advanced}}
                       {:id           "gravsim"
                        :source-paths ["src"]
                        :compiler     {:main          "gravsim.core"
                                       :output-to     "resources/src/gravsim.js"
                                       :output-dir    "resources/optimized/gravsim"
                                       :asset-path    "optimized/gravsim"
                                       :optimizations :advanced}}]}

  ;===== LOCAL-JARS =====;
  :profiles {:corejar   {:main         gravsim.core
                         :uberjar-name "gravsim-standalone.jar"
                         :aot          :all
                         :auto-clean   false}})
