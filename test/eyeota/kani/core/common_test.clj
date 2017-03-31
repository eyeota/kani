(ns eyeota.kani.core.common-test
  (:require [clojure.test :refer :all]
            [eyeota.kani.util.cassandra :refer [embedded-cassandra setup-cassandra *cluster* keyspace-name]]
            [eyeota.kani.core.common :as common]))

(use-fixtures :once embedded-cassandra)
(use-fixtures :each setup-cassandra)

(deftest test-table-names
  (is (= ["tracks_by_album" "playlists" "bands"] (common/table-names *cluster* keyspace-name [])))
  (is (= ["playlists" "bands"] (common/table-names *cluster* keyspace-name ["tracks_by_album"])))
  (is (= ["tracks_by_album"] (common/table-names *cluster* keyspace-name ["playlists" "bands"]))))
