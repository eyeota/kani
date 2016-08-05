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

(ns eyeota.kani.core.schema
  (:require [eyeota.kani.core.common :refer :all])
  (:import (com.datastax.driver.core Session Cluster ResultSetFuture)))

(defn export-keyspace
  ^String [^Cluster cluster keyspace-name]
  (keyspace-cql-string cluster keyspace-name))

(defn import-keyspace
  [^Session session cql-string]
  (let [cql-strings (clojure.string/split cql-string cql-command-separator)
        [init-keyspace-cql & create-tables-cql] cql-strings]
    (.execute session ^String init-keyspace-cql)
    (->> (map #(.executeAsync session ^String %) create-tables-cql)
         (pmap #(.getUninterruptibly ^ResultSetFuture %))
         (doall))))

(defn drop-keyspace
  [^Session session keyspace-name]
  (.execute session (str "DROP KEYSPACE " keyspace-name)))
