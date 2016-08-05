(defproject kani "0.0.1-SNAPSHOT"
  :description "Cassandra CSV export/import. Much better than COPY FROM/TO™"
  :url "https://stash.eyeota.com/projects/UNI/repos/eyeota.kani"
  :license {:name "GNU Lesser General Public License v3.0"
            :url  "https://www.gnu.org/licenses/lgpl-3.0.en.html"}
  :profiles {:uberjar {:aot :all}
             :dev     {:resource-paths ["test/resources"]
                       :dependencies   [[org.cassandraunit/cassandra-unit "3.0.0.1" :exclusions [io.netty/netty-all]]]}
             :6gheap  {:jvm-opts ["-Xms6g" "-Xmx6g"]}}
  :jvm-opts ["-Xms2g" "-Xmx2g" "-Djava.rmi.server.hostname=localhost"]
  :plugins [[lein-libdir "0.1.1"]
            [jonase/eastwood "0.2.3"]
            [lein-bikeshed "0.2.0" :exclusions [org.clojure/tools.namespace]]
            [lein-kibit "0.1.2"]
            [test2junit "1.1.3"]]
  :aot [eyeota.kani.application.main
        eyeota.kani.application.compare-csv
        eyeota.kani.application.schema
        eyeota.kani.application.table]
  :libdir-path "target/lib"
  :jar-name "kani.jar"
  :uberjar-name "kani-standalone.jar"
  :main eyeota.kani.application.main

  :aliases {"quality" ["do"
                       ["eastwood" "{:namespaces [:source-paths] :exclude-linters [:wrong-tag]}"]
                       ["bikeshed" "-m" "120"]
                       ["kibit"]]
            "build"   ["do" ["clean"] ["test2junit"] ["quality"] ["libdir"]]
            "check"   ["do" ["clean"] ["test"] ["quality"]]}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.csv "0.1.3"]
                 [org.clojure/tools.cli "0.3.5"]

                 [com.datastax.cassandra/cassandra-driver-core "3.0.3"]

                 ; for ccassandra + cassandra driver
                 [com.google.guava/guava "18.0"]
                 [org.fusesource/sigar "1.6.4"]

                 [org.slf4j/slf4j-log4j12 "1.7.21"]])
