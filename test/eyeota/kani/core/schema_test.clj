(ns eyeota.kani.core.schema-test
  (:require [clojure.test :refer :all]
            [eyeota.kani.core.common :refer :all]
            [eyeota.kani.util.cassandra :refer :all]
            [eyeota.kani.core.schema :as schema]))

(use-fixtures :once embedded-cassandra)
(use-fixtures :each setup-cassandra)

(deftest import-export-schema
  (with-open [session (.connect *cluster*)]
    (let [keyspace "kani_test"
          exported-cql (schema/export-keyspace *cluster* keyspace)
          _ (do (schema/drop-keyspace session keyspace)
                (schema/import-keyspace session exported-cql))
          re-exported-cql (schema/export-keyspace *cluster* keyspace)]
      (is (= exported-cql re-exported-cql)))))
