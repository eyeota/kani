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

(ns eyeota.kani.application.table
  (:gen-class)
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as string]
            [eyeota.kani.core.common :refer :all]
            [eyeota.kani.application.config :refer :all]
            [eyeota.kani.core.consistency :refer :all]
            [eyeota.kani.core.table :as table]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io])
  (:import (com.datastax.driver.core Cluster)))

(defn export-table
  [{:keys [keyspace hosts port fetch-size table-fetch-size null-value consistency]} table-name csv-file]
  (println "Exporting" table-name)
  (with-open [cluster (build-cluster hosts port fetch-size)
              session (.connect ^Cluster cluster)
              out-file (io/make-writer csv-file file-encoding)]
    (csv/write-csv out-file (table/export-table cluster
                                                session
                                                keyspace
                                                table-name
                                                (get consistency-level consistency)
                                                (get table-fetch-size table-name)
                                                null-value))
    (println "Exporting done")))

(defn import-table
  [{:keys [keyspace hosts port fetch-size null-value consistency]} table-name csv-file]
  (println "Importing" csv-file)
  (with-open [cluster (build-cluster hosts port fetch-size)
              csv-reader (io/make-reader csv-file file-encoding)]
    (let [[column-names & csv-seq] (csv/read-csv csv-reader)]
      (table/import-table cluster
                          keyspace
                          table-name
                          column-names
                          csv-seq
                          (get consistency-level consistency)
                          null-value)
      (println "Importing done"))))

(def cli-options
  [["-c" "--config CONFIG" "Config file"]
   ["-h" "--help"]])

(defn- usage [options-summary]
  (string/join
    \newline
    ["Export / import Cassandra table"
     ""
     "Usage: java -jar kani-standalone.jar -cp eyeota.kani.application.table options action action-argument"
     ""
     "Options:"
     options-summary
     ""
     "Actions:"
     "  export table-name csv-file    Exports Cassandra table"
     "  import table-name csv-file    Imports Cassandra table"
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
    (or (< (count arguments) 2) (> (count arguments) 3)) (exit 1 (usage summary))
    (nil? (:config options)) (exit 1 (usage summary))
    errors (exit 1 (error-msg errors))))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options :strict true)
        _ (check-arguments options arguments errors summary)
        config (read-config (:config options))
        [action first-argument second-argument] arguments]

    (case action
      "export" (export-table config first-argument second-argument)
      "import" (import-table config first-argument second-argument)
      (exit 1 (usage summary)))
    (System/exit 0)))
