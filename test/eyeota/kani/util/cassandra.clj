;; Copyright (C) 2016 Eyeota

;; This file is part of kani.

;; kani is free software:  you can redistribute it and/or modify
;; it under the terms of the GNU Lesser General Public License as published by
;; the Free Software Foundation, version 3 of the License.

;; kani is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU Lesser General Public License for more details.

;; You should have received a copy of the GNU Lesser General Public License
;; along with kani.  If not, see <http://www.gnu.org/licenses/>.

(ns eyeota.kani.util.cassandra
  (:require [eyeota.kani.core.common :refer :all]
            [eyeota.kani.core.schema :as schema]
            [clojure.java.io :as io])
  (:import (org.cassandraunit.utils EmbeddedCassandraServerHelper)
           (com.datastax.driver.core Cluster Session)))

(def ^:dynamic ^Cluster *cluster*)
(def keyspace-name "kani_test")

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
    (let [test-data-cql (-> (io/resource (str keyspace-name "_data.cql"))
                            (slurp)
                            (clojure.string/split cql-command-separator))]
      (schema/import-keyspace session (slurp (io/resource (str keyspace-name ".cql"))))
      (doseq [cql-statement test-data-cql]
        (.execute session ^String cql-statement))
      (f)
      (.execute session (str "DROP KEYSPACE " keyspace-name)))))
