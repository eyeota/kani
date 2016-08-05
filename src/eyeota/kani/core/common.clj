(ns eyeota.kani.core.common
  (:import (com.datastax.driver.core Cluster Metadata KeyspaceMetadata ProtocolOptions TableMetadata QueryOptions)
           (java.nio.charset Charset)
           (com.datastax.driver.core.policies ConstantReconnectionPolicy Policies RetryPolicy ReconnectionPolicy)))

(def utf-8 (Charset/forName "UTF-8"))
(def cql-command-separator #";\n+")
(def comma-separator #", ")
(def file-encoding {:encoding "UTF-8"})

(defn build-cluster
  ^Cluster
  ([addresses port fetch-size]
   {:pre [(coll? addresses) (number? port) (number? fetch-size)]}
   (build-cluster addresses port fetch-size (ConstantReconnectionPolicy. 1000) (Policies/defaultRetryPolicy)))
  ^Cluster
  ([addresses port fetch-size ^ReconnectionPolicy reconnection-policy ^RetryPolicy retry-policy]
   {:pre [(coll? addresses) (number? port) (number? fetch-size)]}
   (let [query-options (.setFetchSize (QueryOptions.) fetch-size)]
     (-> (Cluster/builder)
         (.addContactPoints #^"[Ljava.lang.String;" (into-array String addresses))
         (.withPort (or port ProtocolOptions/DEFAULT_PORT))
         (.withReconnectionPolicy reconnection-policy)
         (.withRetryPolicy retry-policy)
         (.withQueryOptions query-options)
         (.build)))))

(defn cluster-metadata
  ^Metadata [^Cluster cluster]
  (.getMetadata cluster))

(defn keyspace-metadata
  ^KeyspaceMetadata [^Cluster cluster ^String keyspace-name]
  (.getKeyspace (cluster-metadata cluster) keyspace-name))

(defn keyspace-cql-string
  [^Cluster cluster ^String keyspace-name]
  (.exportAsString (keyspace-metadata cluster keyspace-name)))

(defn table-metadata
  ^TableMetadata [^Cluster cluster keyspace-name table-name]
  (-> cluster
      (keyspace-metadata keyspace-name)
      (.getTable table-name)))

(defn table-names
  [^Cluster cluster keyspace-name]
  (->> (keyspace-metadata cluster keyspace-name)
       (.getTables)
       (mapv #(.getName ^TableMetadata %))))
