CREATE KEYSPACE kani_test WITH REPLICATION = { 'class' : 'org.apache.cassandra.locator.SimpleStrategy', 'replication_factor': '1' } AND DURABLE_WRITES = true;

CREATE TABLE kani_test.bands (
    id uuid,
    created timestamp,
    name text,
    description text,

    albums list<text>,
    members set<text>,
    best_hit_by_year map<int, text>,
    total_album_sales bigint,
    id_list list<uuid>,

    ip_address inet,
    photo blob,

    float_value float,
    double_value double,

    PRIMARY KEY (id)
) WITH read_repair_chance = 0.0
   AND dclocal_read_repair_chance = 0.1
   AND gc_grace_seconds = 864000
   AND bloom_filter_fp_chance = 0.01
   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }
   AND comment = ''
   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy' }
   AND compression = { 'sstable_compression' : 'org.apache.cassandra.io.compress.LZ4Compressor' }
   AND default_time_to_live = 0
   AND speculative_retry = '99.0PERCENTILE'
   AND min_index_interval = 128
   AND max_index_interval = 2048;

CREATE TABLE kani_test.playlists (
    artist_id uuid,
    mood text,
    created timeuuid,

    name text,
    songs list<text>,

    PRIMARY KEY ((artist_id, mood), created)
) WITH read_repair_chance = 0.0
   AND dclocal_read_repair_chance = 0.1
   AND gc_grace_seconds = 864000
   AND bloom_filter_fp_chance = 0.01
   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }
   AND comment = ''
   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy' }
   AND compression = { 'sstable_compression' : 'org.apache.cassandra.io.compress.LZ4Compressor' }
   AND default_time_to_live = 0
   AND speculative_retry = '99.0PERCENTILE'
   AND min_index_interval = 128
   AND max_index_interval = 2048;

CREATE TABLE kani_test.tracks_by_album (
    album_title text,
    year int,
    band varchar static,
    genre varchar static,

    number int,
    track_title varchar,

    PRIMARY KEY ((album_title, year), number)
) WITH read_repair_chance = 0.0
   AND dclocal_read_repair_chance = 0.1
   AND gc_grace_seconds = 864000
   AND bloom_filter_fp_chance = 0.01
   AND caching = { 'keys' : 'ALL', 'rows_per_partition' : 'NONE' }
   AND comment = ''
   AND compaction = { 'class' : 'org.apache.cassandra.db.compaction.SizeTieredCompactionStrategy' }
   AND compression = { 'sstable_compression' : 'org.apache.cassandra.io.compress.LZ4Compressor' }
   AND default_time_to_live = 0
   AND speculative_retry = '99.0PERCENTILE'
   AND min_index_interval = 128
   AND max_index_interval = 2048;

