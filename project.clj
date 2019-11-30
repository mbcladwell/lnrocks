(defproject lnrocks "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src/clojure/"]
  :java-source-paths ["src/java"]
   :resource-paths ["resources"]
  :javac-options     ["-target" "1.8" "-source" "1.8"]
 :dependencies [[org.clojure/clojure "1.10.0"]
                 [juxt/crux-core "RELEASE"]
                [juxt/crux-rocksdb "19.09-1.5.0-alpha"]
                 [com.google.guava/guava "23.0"]
                 [org.apache/poi "4.0.1"]
                 [org.apache/poi-ooxml "4.0.1"]
                 [org.apache.xmlbeans/xmlbeans "3.1.0"]
                 [org.apache.commons/commons-collections4 "4.2"]
                 [org.apache.commons/commons-compress"1.18"]
                 [org.apache.poi/ooxml-schemas "1.4"]
               ;; [incanter/incanter-core "1.9.4"]
     
                ]
:repl-options {:nrepl-middleware
                 [cider.nrepl.middleware.apropos/wrap-apropos
                  cider.nrepl.middleware.classpath/wrap-classpath
                  cider.nrepl.middleware.complete/wrap-complete
                  cider.nrepl.middleware.info/wrap-info
                  cider.nrepl.middleware.inspect/wrap-inspect
                  cider.nrepl.middleware.macroexpand/wrap-macroexpand
                  cider.nrepl.middleware.ns/wrap-ns
                  cider.nrepl.middleware.resource/wrap-resource
                  cider.nrepl.middleware.stacktrace/wrap-stacktrace
                  cider.nrepl.middleware.test/wrap-test
                  cider.nrepl.middleware.trace/wrap-trace
                  cider.nrepl.middleware.undef/wrap-undef]}
  :manifest {"Permissions" "all-permissions"
             "Codebase" "http://www.labsolns.com/software/webstart"
             "Application-Name" "LIMSNucleus-KV"
             "Application-title" "LIMSNucleus-KV"
             }

  :main ^:skip-aot lnrocks.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
