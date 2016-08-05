(ns eyeota.kani.util.cassandra
  (:require [eyeota.kani.core.common :refer :all]
            [eyeota.kani.core.schema :as schema]
            [clojure.java.io :as io])
  (:import (org.cassandraunit.utils EmbeddedCassandraServerHelper)
           (com.datastax.driver.core Cluster Session)))

(def ^:dynamic ^Cluster *cluster*)

(defn embedded-cassandra [f]
  (EmbeddedCassandraServerHelper/startEmbeddedCassandra "cassandra.yml" 40000)
  (binding [*cluster* (build-cluster ["127.0.0.1"] 19142 2000)]
    (try
      (f)
      (finally
        (.close *cluster*)
        (EmbeddedCassandraServerHelper/cleanEmbeddedCassandra)))))

(defn setup-cassandra [f]
  (with-open [session ^Session (.connect *cluster*)]
    (let [test-data-cql (-> (io/resource "kani_test_data.cql")
                            (slurp)
                            (clojure.string/split cql-command-separator))]
      (schema/import-keyspace session (slurp (io/resource "kani_test.cql")))
      (doseq [cql-statement test-data-cql]
        (.execute session ^String cql-statement))
      (f)
      (.execute session "DROP KEYSPACE kani_test"))))
