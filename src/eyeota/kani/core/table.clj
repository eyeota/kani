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

(ns eyeota.kani.core.table
  (:require [eyeota.kani.core.common :refer :all]
            [eyeota.kani.core.encoding :as encoding]
            [eyeota.kani.core.column :as column])
  (:import (com.datastax.driver.core Row ResultSet Cluster ConsistencyLevel SimpleStatement
                                     ResultSetFuture BoundStatement Session PreparedStatement)
           (java.util List Set Map)))

(defn- row->str
  [^Row row columns null-value]
  (loop [[col & remaining-cols] columns
         result (transient [])]
    (if col
      (recur remaining-cols
             (conj! result (encoding/value row col null-value)))
      (persistent! result))))

(defn- rows->str
  [^ResultSet rows columns null-value]
  (map #(row->str % columns null-value) rows))

(defn export-table
  "Exports Cassandra table into CSV format. Returns lazy sequence of CSV entries (first entry is column names)"
  [^Cluster cluster ^Session session keyspace-name table-name ^ConsistencyLevel consistency-level fetch-size null-value]
  (let [table (table-metadata cluster keyspace-name table-name)
        columns (mapv column/column-metadata (.getColumns table))
        statement (-> (format "SELECT * FROM %s.%s;" keyspace-name table-name)
                      (SimpleStatement.)
                      (.setConsistencyLevel consistency-level))
        statement-with-fetch-size (if fetch-size
                                    (.setFetchSize statement fetch-size)
                                    statement)
        rows (.execute session statement-with-fetch-size)
        column-names (mapv :name columns)]
    (cons column-names (rows->str rows columns null-value))))

(defn- bind-bound-statement
  [columns-by-name ^BoundStatement bound-statement [^String col-name col-value]]
  (if-not (nil? col-value)
    (let [col (first (get columns-by-name col-name))
          [col-first-arg col-second-arg] (:arguments col)]
      (condp = (:type col)
        :list (.setList bound-statement col-name ^List col-value (column/column-type->class col-first-arg))
        :set (.setSet bound-statement col-name ^Set col-value (column/column-type->class col-first-arg))
        :map (.setMap bound-statement col-name ^Map col-value
                      (column/column-type->class col-first-arg)
                      (column/column-type->class col-second-arg))
        (.set bound-statement col-name col-value (column/column-type->class (:type col)))))
    ; requires setToNull for CQL v3-compliant (Cassandra 2.1)
    (.setToNull bound-statement col-name)))

(defn- execute-cql
  [^Session session ^PreparedStatement prepared-statement columns-by-name column-names cql-values]
  (let [bound-statement (BoundStatement. prepared-statement)
        col-name-value-mapping (zipmap column-names cql-values)
        bound-statement ^BoundStatement (reduce (partial bind-bound-statement columns-by-name)
                                                bound-statement
                                                col-name-value-mapping)]
    (.executeAsync session bound-statement)))

(defn- process-csv-cell
  [columns-by-name columns-list null-value idx csv-cell]
  (let [column-name (nth columns-list idx)
        col-metadata (first (get columns-by-name column-name))]
    (encoding/parse csv-cell col-metadata null-value)))

(defn import-table
  "Imports the CSV lazy sequence into Cassandra. Returns the number of rows in the CSV sequence"
  [^Cluster cluster keyspace-name table-name column-names csv-seq ^ConsistencyLevel consistency-level null-value]
  (with-open [session (.connect cluster)]
    (let [table (table-metadata cluster keyspace-name table-name)
          columns (mapv column/column-metadata (.getColumns table))
          columns-by-name (group-by :name columns)
          columns-names-str (clojure.string/join ", " column-names)
          columns-values-str (clojure.string/join ", " (repeat (count column-names) "?"))
          prepared-statement (-> session
                                 (.prepare (format "INSERT INTO %s.%s (%s) VALUES (%s);"
                                                   keyspace-name
                                                   table-name
                                                   columns-names-str
                                                   columns-values-str))
                                 (.setConsistencyLevel consistency-level))
          process-csv-xf (comp
                           (map #(map-indexed (partial process-csv-cell columns-by-name column-names null-value) %))
                           (map (partial execute-cql session prepared-statement columns-by-name column-names)))
          result-set-futures (into [] process-csv-xf csv-seq)]
      (doseq [^ResultSetFuture result-set-future result-set-futures]
        (.getUninterruptibly result-set-future))
      (count result-set-futures))))
