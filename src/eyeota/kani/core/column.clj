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

(ns eyeota.kani.core.column
  (:import (java.util UUID Set Map List Date)
           (java.net InetAddress)
           (com.datastax.driver.core ColumnMetadata DataType DataType$CollectionType)
           (java.nio ByteBuffer)))

(defn column-metadata
  "Wraps ColumnMetadata to our own custom data structure
  ```
  {:name  \"column-name\"
   :type  :text}
  ```

  or

  ```
  {:name       \"column-name\"
   :type       :map
   :arguments  [:int :text]
  ```
  "
  [^ColumnMetadata column]
  (let [data-type ^DataType (.getType column)
        column-result {:name (.getName column)
                       :type (keyword (str (.getName data-type)))}]
    (if (instance? DataType$CollectionType data-type)
      (let [column-type-args (.getTypeArguments data-type)
            column-arguments (mapv #(keyword (str (.getName %))) column-type-args)]
        (assoc column-result :arguments column-arguments))
      column-result)))

(defn column-type->class
  "Returns the Java class based on the given column type"
  ^Class [col-type]
  (condp = col-type
    :int Integer
    :bigint Long
    :double Double
    :float Float
    :boolean Boolean

    :varint BigInteger
    :decimal BigDecimal

    :blob ByteBuffer
    :inet InetAddress
    :text String
    :uuid UUID
    :timeuuid UUID
    :timestamp Date

    :list List
    :set Set
    :map Map))
