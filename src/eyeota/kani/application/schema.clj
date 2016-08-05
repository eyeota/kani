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

(ns eyeota.kani.application.schema
  (:gen-class)
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as string]
            [eyeota.kani.core.common :refer :all]
            [eyeota.kani.application.config :refer :all]
            [eyeota.kani.core.schema :as schema])
  (:import (com.datastax.driver.core Cluster)))

(defn export-schema-definition
  "Exports schema definition into a CQL file"
  [{:keys [keyspace hosts port fetch-size]} cql-file]
  (println "Exporting" cql-file)
  (with-open [cluster (build-cluster hosts port fetch-size)]
    (spit cql-file (schema/export-keyspace cluster keyspace))
    (println "Exporting done")))

(defn import-schema-definition
  "Imports schema definition from a CQL file into Cassandra database"
  [{:keys [hosts port fetch-size]} cql-file]
  (println "Importing" cql-file)
  (with-open [cluster (build-cluster hosts port fetch-size)
              session (.connect ^Cluster cluster)]
    (schema/import-keyspace session (slurp cql-file))
    (println "Importing done")))

(def cli-options
  [["-c" "--config CONFIG" "Config file"]
   ["-f" "--file CQL-FILE" "CQL File"]
   ["-h" "--help"]])

(defn- usage [options-summary]
  (string/join
    \newline
    ["Export / import specific Cassandra schema/keyspace"
     ""
     "Usage: java -jar kani-standalone.jar -cp eyeota.kani.application.schema options action"
     ""
     "Options:"
     options-summary
     ""
     "Actions:"
     "  export   Exports Cassandra schema definition"
     "  import   Imports Cassandra schema definition"
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
    (or (nil? (:config options)) (nil? (:file options))) (exit 1 (usage summary))
    errors (exit 1 (error-msg errors))))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options :strict true)
        _ (check-arguments options arguments errors summary)
        config (read-config (:config options))
        cql-file (:file options)]
    (case (first arguments)
      "export" (export-schema-definition config cql-file)
      "import" (import-schema-definition config cql-file)
      (exit 1 (usage summary)))
    (System/exit 0)))
