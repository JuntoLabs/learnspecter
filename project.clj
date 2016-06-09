(defproject junto-labs/learn-specter "0.0.1-SNAPSHOT"
  :description "A repo for... learning Specter!"
  :jvm-opts ^:replace
    ["-XX:-OmitStackTraceInFastThrow"
     "-d64" "-server"]
  :plugins [[lein-environ  "1.0.3" ]
            [lein-essthree "0.2.1" ]
            [lein-ancient  "0.6.10"]]
  :dependencies
    [[org.clojure/clojure                       "1.9.0-alpha5"    ]
     [org.clojure/clojurescript                 "1.9.36"          ]
     ; ==== NAMESPACE ====
     [org.clojure/tools.namespace               "0.2.11"          ]
     ; ==== ASYNC ====
     [org.clojure/core.async                    "0.2.374"         ]
     [re-frame                                  "0.7.0"           ]
     [reagent                                   "0.5.1"
       :exclusions [org.json/json]                                ]
     [com.taoensso/sente                        "1.8.1"           ]
     [com.taoensso/timbre                       "4.3.1"           ]
     ; ==== RESOURCES ====
     [com.stuartsierra/component                "0.3.1"           ]
     [clj-http                                  "3.1.0"
       :exclusions [org.json/json
                    com.fasterxml.jackson.core/jackson-core
                    commons-codec
                    potemkin]                                     ]
     [compojure                                 "1.5.0"
       :exclusions [clj-http]                                     ]
     [aleph                                     "0.4.1"           ]
     ; ==== AUTH ====  
     [ring/ring-defaults                        "0.2.0"           ]
     [bk/ring-gzip                              "0.1.1"           ]]
   :profiles
   {:dev {:injections [] ; (clojure.main/repl :print clojure.pprint/pprint)
          :resource-paths ["dev-resources"]
          :dependencies   [[figwheel "0.5.3-2"]]
          :plugins [[lein-cljsbuild "1.1.3"]
                    [lein-figwheel  "0.5.3-2"
                      :exclusions [org.clojure/clojure
                                   org.clojure/core.async]]]}}
  :aliases {"all" ["with-profile" "dev:dev,1.5:dev,1.7"]
            "deploy-dev"      ["do" "clean," "install"]
            "deploy-prod"     ["do" "clean," "install," "deploy" "clojars"]
            "deploy-test-dev" ["do" "clean," "cljsbuild" "once" "dev"]
            "autobuilder"     ["do" "clean," "figwheel" "dev"]
            "test"            ["do" "clean," "test," "with-profile" "dev" "cljsbuild" "test"]}
  :auto-clean  false
  :target-path "target"
  :clean-targets ^{:protect false} [:target-path
                                    [:cljsbuild :builds :dev :compiler :output-dir]
                                    [:cljsbuild :builds :dev :compiler :output-to ]
                                    [:cljsbuild :builds :min :compiler :output-dir]
                                    [:cljsbuild :builds :min :compiler :output-to ]]
  :java-source-paths ["src/java"]
  :source-paths      ["src/clj"
                      "src/cljc"]
  :test-paths     ["test"]
  :global-vars {*warn-on-reflection* true
                *unchecked-math*     :warn-on-boxed}
  :cljsbuild
    {:builds
      {:dev
        {:figwheel true
         :source-paths ["test/cljs" "src/cljc" "dev/cljc"]
         :compiler {:output-to            "dev-resources/public/js/compiled/quantum.js"
                    :output-dir           "dev-resources/public/js/compiled/out"
                    :optimizations        :none
                    :main                 quantum.dev
                    :asset-path           "js/compiled/out"
                    :source-map           true
                    :source-map-timestamp true
                    :cache-analysis       true}}
       :min
         {:source-paths ["src/cljc" "dev/cljc"]
          :compiler {:output-to      "dev-resources/public/js/min-compiled/quantum.js"
                     :output-dir     "dev-resources/public/js/min-compiled/out"
                     :main           quantum.dev
                     :optimizations  :advanced
                     :asset-path     "js/min-compiled/out"
                     :pretty-print   false
                     ;:parallel-build true
                     }}}}
  :figwheel {:http-server-root "public" ;; default and assumes "resources" 
             :server-port      3449
             :css-dirs         ["dev-resources/public/css"]}
  )