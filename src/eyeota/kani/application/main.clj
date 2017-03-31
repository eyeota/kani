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

(ns eyeota.kani.application.main
  (:gen-class)
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as string]
            [eyeota.kani.core.common :refer :all]
            [eyeota.kani.application.config :refer :all]
            [eyeota.kani.core.consistency :refer :all]
            [eyeota.kani.core.table :as table]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [eyeota.kani.core.schema :as schema])
  (:import (com.datastax.driver.core Cluster)
           (java.io BufferedReader)))

(defn export-db
  "Exports both schema definition for the keyspace and the contents of all the tables inside the keyspace into a
  separate CQL file and CSV files in the specified directory"
  [{:keys [keyspace hosts port fetch-size table-fetch-size null-value consistency excluded-tables]} directory]
  (println (format "\nExporting to '%s'" directory))
  (with-open [cluster (build-cluster hosts port fetch-size)]
    (print "  Exporting schema... ")
    (flush)
    (let [cql-filename (format "%s/%s.cql" directory keyspace)]
      (io/make-parents cql-filename)
      (spit cql-filename (schema/export-keyspace cluster keyspace)))
    (println "✓ [ok]")
    (doseq [table-name (table-names cluster keyspace excluded-tables)
            :let [csv-filename (format "%s/%s.%s.csv" directory keyspace table-name)]]
      (print (format "  Exporting %s... " table-name))
      (flush)
      (io/make-parents csv-filename)
      (with-open [out-file (io/make-writer csv-filename file-encoding)
                  session (.connect cluster)]
        (let [exported-seq (table/export-table cluster
                                               session
                                               keyspace
                                               table-name
                                               (get consistency-level consistency)
                                               (get table-fetch-size table-name)
                                               null-value)]
          (csv/write-csv out-file exported-seq)))
      (with-open [exported-file (io/make-reader csv-filename file-encoding)]
        (let [rows (-> (.lines ^BufferedReader exported-file)
                       (.count)
                       (dec))]
          (println (format "✓ [%,d rows] [ok]" rows)))))
    (println "Exporting done\n")))

(defn import-db
  "Import both schema definition and the contents of all the tables inside the schema from the CQL and CSV files in the
  specified directory into Cassandra database"
  [{:keys [keyspace hosts port fetch-size null-value consistency excluded-tables]} directory]
  (println (format "\nImporting from '%s'" directory))
  (with-open [cluster (build-cluster hosts port fetch-size)
              session (.connect ^Cluster cluster)]
    (print "  Importing schema... ")
    (flush)
    (schema/import-keyspace session (slurp (format "%s/%s.cql" directory keyspace)))
    (println "✓ [ok]")
    (doseq [table-name (table-names cluster keyspace excluded-tables)
            :let [csv-file (format "%s/%s.%s.csv" directory keyspace table-name)]]
      (print (format "  Importing %s... " table-name))
      (flush)
      (with-open [csv-reader (io/make-reader csv-file file-encoding)]
        (let [[column-names & csv-seq] (csv/read-csv csv-reader)
              rows-imported (table/import-table cluster
                                                keyspace
                                                table-name
                                                column-names
                                                csv-seq
                                                (get consistency-level consistency) null-value)]
          (println (format "✓ [%,d rows] [ok]" rows-imported)))))
    (println "Importing done\n")))

(def cli-options
  [["-c" "--config CONFIG" "Config file"]
   ["-d" "--directory DIRECTORY" "Directory to store/read files"
    :default "data"]
   ["-h" "--help"]])

(defn- usage [options-summary]
  (string/join
    \newline
    ["Export / import Cassandra keyspace and tables"
     ""
     "Usage: java -jar kani-standalone.jar options actions"
     ""
     "Options:"
     options-summary
     ""
     "Actions:"
     "  export    Exports Cassandra table"
     "  import    Imports Cassandra table"
     ""]))

(defn- error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn- exit [status msg]
  (println msg)
  (System/exit status))

(defn- check-arguments
  [options arguments errors summary]
  (cond
    (:help options) (exit 0 (usage summary))
    (not= (count arguments) 1) (exit 1 (usage summary))
    (nil? (:config options)) (exit 1 (usage summary))
    errors (exit 1 (error-msg errors))))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options :strict true)
        _ (check-arguments options arguments errors summary)
        config (read-config (:config options))
        action (first arguments)]

    (case action
      "export" (export-db config (:directory options))
      "import" (import-db config (:directory options))
      (exit 1 (usage summary)))
    (System/exit 0)))
