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

(ns eyeota.kani.application.compare-csv
  (:gen-class)
  (:require [clojure.tools.cli :as cli]
            [clojure.string :as string]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.set :refer [difference]])
  (:import (java.io File)))

(def check "✓ [ok]")
(def cross "✗ [mismatch]")

(defn- csv-file-list
  [dir]
  (->> (file-seq (io/file dir))
       (filter #(and (string/ends-with? (.getName ^File %) ".csv") (not (.isDirectory ^File %))))
       (vec)))

(defn- file-names-match?
  [files-1 files-2]
  (let [file-names-1 (set (map #(.getName ^File %) files-1))
        file-names-2 (set (map #(.getName ^File %) files-2))]
    (= file-names-1 file-names-2)))

(defn compare-csv-in-dirs
  "Compares the CSV files located in both two directories"
  [dir-1 dir-2]
  (println (format "\nComparing CSV files at '%s' and '%s'" dir-1 dir-2))
  (let [files-1 (csv-file-list dir-1)
        files-2 (csv-file-list dir-2)]
    (print "  Comparing file names... ")
    (if (file-names-match? files-1 files-2)
      (do
        (println check)
        (doseq [[file-1 file-2] (map list files-1 files-2)]
          (print (format "  Comparing %s... " (.getName ^File file-1)))
          (flush)
          (let [csv-1 (set (csv/read-csv (slurp file-1)))
                csv-2 (set (csv/read-csv (slurp file-2)))]
            (if (= csv-1 csv-2)
              (println check)
              (println cross)))))
      (println cross))
    (println "Done\n")))

(def cli-options
  [["-h" "--help"]])

(defn- usage [options-summary]
  (string/join
    \newline
    ["Compares if the CSV files are the same (ignoring the order)"
     ""
     "Usage: java -jar kani-standalone.jar -cp eyeota.kani.application.compare-csv options directory-1 directory-2"
     ""
     "Options:"
     options-summary
     ""
     "Actions:"
     "  directory-1    Directory to be compared"
     "  directory-2    Directory to be compared"
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
    (not= (count arguments) 2) (exit 1 (usage summary))
    errors (exit 1 (error-msg errors))))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options :strict true)
        _ (check-arguments options arguments errors summary)
        [dir-1 dir-2] args]
    (compare-csv-in-dirs dir-1 dir-2)
    (System/exit 0)))
