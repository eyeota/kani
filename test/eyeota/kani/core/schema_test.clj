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
