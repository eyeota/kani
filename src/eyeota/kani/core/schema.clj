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
