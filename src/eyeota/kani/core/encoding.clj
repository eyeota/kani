(ns eyeota.kani.core.encoding
  (:require [eyeota.kani.core.common :refer :all]
            [eyeota.kani.core.column :as column])
  (:import (javax.xml.bind DatatypeConverter)
           (java.nio.charset Charset)
           (java.util HashSet ArrayList Collection Map$Entry HashMap Map Date UUID)
           (com.datastax.driver.core Row)
           (java.net InetAddress)
           (java.nio ByteBuffer)))

(defn str->coll-of-str
  "Converts a string into a collection of strings. It expects an input that has the following pattern
  \"[item-1, item-2, item-3]\""
  [^String value]
  (-> value
      (subs 1 (dec (count value)))
      (clojure.string/split comma-separator)))

(defmulti value
          "Returns the value from the given Cassandra Row and column (where column is our custom data structure based on
          Cassandra's ColumnMetadata.

          If the value type is a `String` this function will call `str->hex` method which encodes the string value into
          a hexadecimal format in order to be able safely store it into CSV format

          If the value type is `list`, `map`, or `set` it will call `list->str`, `map->str` and `set->str` accordingly.
          This will in turn converts string into hexadecimal format (if the collection contains any string value)"
          (fn [^Row row column _] (when-not (.isNull row ^String (:name column))
                                    (:type column))))

(defmulti parse
          "Parses the value from string into the appropriate Java Object. If the target object is a String, then it
          will first decode from hexadecimal back into the original string. The same with collection that has string
          value in it"
          (fn [^String value column null-value]
            (when-not (= null-value value)
              (:type column))))

(defn str->hex
  [text]
  (when text
    (DatatypeConverter/printHexBinary (.getBytes text utf-8))))

(defn hex->str
  [value]
  (when value
    (String. (DatatypeConverter/parseHexBinary value) ^Charset utf-8)))

(defn list->str
  [^Row row column]
  (let [classname (column/column-type->class (first (:arguments column)))
        result-list (.getList row ^String (:name column) classname)]
    (when result-list
      (if (= classname String)
        (ArrayList. ^Collection (map str->hex result-list))
        result-list))))

(defn set->str
  [^Row row column]
  (let [classname (column/column-type->class (first (:arguments column)))
        result-set (.getSet row ^String (:name column) classname)]
    (when result-set
      (if (= classname String)
        (HashSet. ^Collection (map str->hex result-set))
        result-set))))

(defn- encode-map
  [key-class val-class ^Map$Entry map-entry]
  (let [key-encoded (if (= key-class String)
                      (str->hex (.getKey map-entry))
                      (.getKey map-entry))
        val-encoded (if (= val-class String)
                      (str->hex (.getValue map-entry))
                      (.getValue map-entry))]
    [key-encoded val-encoded]))

(defn map->str
  [^Row row column]
  (let [key-class (column/column-type->class (first (:arguments column)))
        val-class (column/column-type->class (second (:arguments column)))
        result-map (.getMap row ^String (:name column) key-class val-class)]
    (when result-map
      (let [transformed-map (mapcat (partial encode-map key-class val-class) result-map)]
        (HashMap. ^Map (apply hash-map transformed-map))))))

(defmethod value nil [_ _ null-value] null-value)

(defmethod value :int
  [^Row row column _]
  (.getInt row ^String (:name column)))
(defmethod value :bigint
  [^Row row column _]
  (.getLong row ^String (:name column)))
(defmethod value :double
  [^Row row column _]
  (.getDouble row ^String (:name column)))
(defmethod value :float
  [^Row row column _]
  (.getFloat row ^String (:name column)))
(defmethod value :boolean
  [^Row row column _]
  (.getBool row ^String (:name column)))

(defmethod value :varint
  [^Row row column _]
  (.getVarint row ^String (:name column)))
(defmethod value :decimal
  [^Row row column _]
  (.getDecimal row ^String (:name column)))

(defmethod value :inet
  [^Row row column _]
  (.getHostAddress ^InetAddress (.getInet row ^String (:name column))))
(defmethod value :uuid
  [^Row row column _]
  (.getUUID row ^String (:name column)))
(defmethod value :timestamp
  [^Row row column _]
  (.getTime (.getTimestamp row ^String (:name column))))
(defmethod value :timeuuid
  [^Row row column _]
  (.getUUID row ^String (:name column)))
(defmethod value :text
  [^Row row column _]
  (str->hex (.getString row ^String (:name column))))
(defmethod value :blob
  [^Row row column _]
  (->> (.getBytes row ^String (:name column))
       (.array)
       (DatatypeConverter/printHexBinary)))

(defmethod value :list
  [^Row row column _]
  (list->str row column))
(defmethod value :map
  [^Row row column _]
  (map->str row column))
(defmethod value :set
  [^Row row column _]
  (set->str row column))

(defn str->list
  [^String value column]
  (let [list-contents (str->coll-of-str value)
        coll-class (first (:arguments column))]
    (if (= coll-class :text)
      (ArrayList. ^Collection (map hex->str list-contents))
      (ArrayList. ^Collection (map #(parse % {:type coll-class} nil) list-contents)))))

(defn str->set
  [^String value column]
  (let [set-contents (str->coll-of-str value)
        coll-class (first (:arguments column))]
    (if (= coll-class :text)
      (HashSet. ^Collection (map hex->str set-contents))
      (HashSet. ^Collection (map #(parse % {:type coll-class} nil) set-contents)))))

(def ^:private map-separator #"=")
(defn- decode-map
  [key-class val-class map-content]
  (let [splitted (clojure.string/split map-content map-separator)
        key-decoded (if (= key-class :text)
                      (hex->str (first splitted))
                      (parse (first splitted) {:type key-class} nil))
        val-decoded (if (= val-class :text)
                      (hex->str (second splitted))
                      (parse (second splitted) {:type val-class} nil))]
    [key-decoded val-decoded]))

(defn str->map
  [^String value column]
  (let [map-contents (str->coll-of-str value)
        key-class (first (:arguments column))
        val-class (second (:arguments column))
        map-decoded (mapcat (partial decode-map key-class val-class) map-contents)]
    (HashMap. ^Map (apply hash-map map-decoded))))

(defmethod parse nil [_ _ _] nil)

(defmethod parse :int
  [^String value _ _]
  (Integer/parseInt value 10))
(defmethod parse :bigint
  [^String value _ _]
  (Long/parseLong value 10))
(defmethod parse :double
  [^String value _ _]
  (Double/parseDouble value))
(defmethod parse :float
  [^String value _ _]
  (Float/parseFloat value))
(defmethod parse :boolean
  [^String value _ _]
  (Boolean/parseBoolean value))

(defmethod parse :varint
  [^String value _ _]
  (BigInteger. value 10))
(defmethod parse :decimal
  [^String value _ _]
  (BigDecimal. value))

(defmethod parse :uuid
  [^String value _ _]
  (UUID/fromString value))
(defmethod parse :timeuuid
  [^String value _ _]
  (UUID/fromString value))
(defmethod parse :timestamp
  [^String value _ _]
  (Date. (Long/parseLong value 10)))
(defmethod parse :inet
  [^String value _ _]
  (InetAddress/getByName value))
(defmethod parse :text
  [^String value _ _]
  (hex->str value))
(defmethod parse :blob
  [^String value _ _]
  (ByteBuffer/wrap (DatatypeConverter/parseHexBinary value)))

(defmethod parse :list
  [^String value column _]
  (str->list value column))
(defmethod parse :map
  [^String value column _]
  (str->map value column))
(defmethod parse :set
  [^String value column _]
  (str->set value column))
