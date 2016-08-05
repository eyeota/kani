# kani

Cassandra CSV export/import. Better than cqlsh `COPY FROM/TO`. Requires Java 8

Tested to work on Cassandra 2.1.15 and 2.2.4

## Why?
Because `COPY FROM/TO` did not work for escaped characters (or any unicode). It also produces invalid CSV format if you
have stringified JSON as a value.

Suppose you have the value of `"ABC\nDEF"` where `ABC` and `DEF` are splitted using newline character. What `COPY TO`
does is to store it as a literal `"ABC\nDEF"` in the CSV. However, when `COPY FROM` reads it, it ignoes `\` character -
so the value restored will be `ABCnDEF`.

Another example is a stringified JSON value if you have `"{"list": [1,2,3]}"` as a value, `COPY TO` will store it as
`"{\"list\": [1,2,3]}"` which doesn't work with csv readers.

How kani solves this issue is simple: instead of escaping characters or dealing with quotes, we simply store any text
values in hexadecimal format.

## Limitations
No support for Cassandra custom type and `COUNTER`

## Usage

### Using jar build

#### Export / import both schema and tables for a specific keyspace

```
java -Xmx2g -Xms2g -jar kani-standalone.jar -c "config.edn" [-d "data"] (export | import)
```

Options:

* `-h --help`
* `-c --config` - EDN config files
* `-d --directory` (optional) - directory where export/import will write/read files to/from

Note:
Recommended heap size is about 2GB

#### Export / import schema only

```
java -Xmx2g -Xms2g -cp kani-standalone.jar eyeota.kani.application.schema -c "config.edn" -f "file.cql" (export | import)
```

Options:

* `-h -- help`
* `-c --config` - EDN config file
* `-f --file` - CQL file to be exported/imported to/from

#### Export / import specific table

```
java -Xmx2g -Xms2g -cp kani-standalone.jar eyeota.kani.application.table -c "config.edn" (export <table name> | import <table name> <csv file>)
```

Options:

* `-h -- help`
* `-c --config` - EDN config file

#### Comparing CSV files
Useful to test if import/export works as expected, for example: db can be exported first to a directory
`data-original`, then db is then imported from `data-original` and then re-exported to `data-re-exported`. Once that's
done, CSV files in both `data-original` and `data-re-exported` can be compared (simply using diff might not work
because row ordering might have changed)

```
java -Xmx6g -Xms6g -cp kani-standalone.jar eyeota.kani.application.compare_csv directory-1 directory-2
```

OR use the provided shell script

```
./compare-csv.sh directory-1 directory-2
```

Note:
Comparinvg CSV will require much more memory than export / import, it's a good idea to specify 2-3x more heap space than
export (just in case).

Using the shell script uses less memory but takes a little bit longer to complete

### EDN Config files
```
{:port              9042                ; Cassandra native port to connect to (default: 9042)
 :hosts             ["127.0.0.1"]       ; Cassandra hosts (can specify multiple hosts)
 :keyspace          "db_keyspace"       ; Keyspace to work on
 :fetch-size        2000                ; Cassandra fetch size (if not specified, default to 5000)
 :null-value        "<null>"            ; What value should be stored in CSV for null values (default: "<null>")
 :table-fetch-size  {"huge-table" 20}}  ; Table-specific fetch size (if the row is huge, might want to reduce the
                                        ; number to avoid timeouts)
 :consistency       :quorum             ; Cassandra read/write consistency level (default: quorum). Accepted values are:
                                        ; [:all :any :each-quorum :local-one :local-quorum :local-serial :one :quorum
                                        ;  :serial :three: two]
```

### Using leiningen

By default running through leiningen will set the heap size to 2gb

#### Export / import both schema and tables for a specific keyspace

```
lein run -c "config.edn" [-d "data"] (export | import)
```

#### Export / import schema only

```
lein run -m eyeota.kani.application.schema -c "config.edn" -f "file.cql" (export | import)
```

#### Export / import specific table

```
lein run -m eyeota.kani.application.table -c "config.edn" (export <table-name> | import <table-name> <csv-file>)
```

#### Comparing CSV files
```
lein with-profile +6gheap run -m eyeota.kani.application.compare-csv directory-1 directory-2
```

## Build

### Test
`lein check` - runs both code quality and tests

`lein quality` - runs only code quality checks

`lein test` - runs test

### Build
`lein build` - runs test and copies libraries into `target`

`lein jar` - creates jar file

`lein uberjar` - creates self-contained jar

## License

Copyright © 2016 [Eyeota](https://www.eyeota.com/)

Distributed under the [GNU Lesser General Public License v3.0](https://www.gnu.org/licenses/lgpl-3.0.en.html)
