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
