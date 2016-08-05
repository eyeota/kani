(ns eyeota.kani.core.table-test
  (:require [clojure.test :refer :all]
            [eyeota.kani.core.common :refer :all]
            [eyeota.kani.util.cassandra :refer :all]
            [eyeota.kani.core.table :as table]
            [eyeota.kani.core.schema :as schema]
            [clojure.data.csv :as csv])
  (:import (com.datastax.driver.core ConsistencyLevel)
           (java.io StringWriter)))

(use-fixtures :once embedded-cassandra)
(use-fixtures :each setup-cassandra)

(def ^:private null "<null>")

(defn- import-export-table
  [keyspace table-name]
  (with-open [session (.connect *cluster*)
              exported-string-writer (StringWriter.)
              re-exported-string-writer (StringWriter.)]
    (let [exported-schema-cql (schema/export-keyspace *cluster* keyspace)
          consistency-one ConsistencyLevel/ONE
          _ (->> (table/export-table *cluster* session keyspace table-name consistency-one nil null)
                 (csv/write-csv exported-string-writer))

          [imported-column-names & imported-csv-seq] (csv/read-csv (.toString exported-string-writer))

          _ (do (schema/drop-keyspace session keyspace)
                (schema/import-keyspace session exported-schema-cql)
                (table/import-table *cluster*
                                    keyspace
                                    table-name
                                    imported-column-names
                                    imported-csv-seq
                                    consistency-one
                                    null))

          re-exported-table-csv-seq (table/export-table *cluster* session keyspace table-name consistency-one nil null)
          _ (csv/write-csv re-exported-string-writer re-exported-table-csv-seq)]
      {:exported (str exported-string-writer) :re-exported (str re-exported-string-writer)})))

(deftest test-import-export-table
  (let [keyspace "kani_test"
        bands-table-result (import-export-table keyspace "bands")
        tracks-by-album-table-result (import-export-table keyspace "tracks_by_album")
        playlists-table-result (import-export-table keyspace "playlists")]
    (is (= (:exported bands-table-result) (:re-exported bands-table-result)))
    (is (= (:exported tracks-by-album-table-result) (:re-exported tracks-by-album-table-result)))
    (is (= (:exported playlists-table-result) (:re-exported playlists-table-result)))))
