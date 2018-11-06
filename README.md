# asyncdb

The `asyncdb` library is an asynchronous Netty-based database client for Java. It currently 
only supports MySQL, but will be extended to Postgres and possibly other databases, if/when
there is sufficient interest.

It has no dependencies other than Netty and SLF4J.

## Installation

For Gradle:

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.mslenc:asyncdb:1.1.1'
}
```

See the [jitpack.io site](https://jitpack.io/#mslenc/asyncdb/1.1.1) for other build systems.

## Getting started / connecting

To start working with `asyncdb`, you must first create a `DbConfig` configuration object.
Here's a short example:

```java
DbConfig config =
    DbConfig.newBuilder(MYSQL).
        setHost("127.0.0.1").
        setDefaultCredentials("user", "password").
        setDefaultDatabase("asyncdb").
    build();
```

After you have the configuration, you can create a `DbDataSource` from it:

```java
DbDataSource dataSource = config.makeDataSource();
```

To finally obtain connections, call one of the connect methods on the `DbDataSource`:

```java
// to use the default credentials and database from config:
dataSource.connect();

// to connect to a different database:
dataSource.connect("otherdb");

// to use different credentials altogether:
dataSource.connect("someuser", "somepass", "somedb");
```

The `connect()` methods return a `CompletableFuture` (as do all other async methods in the library).

It is possible to limit the total number of connections to the server with
```
configBuilder.setMaxTotalConnections(150);
```
When the total number of connections reaches the limit, further attempts to connect will
be placed into a queue, waiting until one of the existing connections is closed.

It is also possible to use the `DbDataSource` as a connection pool, by simply
setting:
```
configBuilder.setMaxIdleConnections(30);
```
This will cause the TCP connections to be re-used, re-initializing and/or re-authorizing 
them if/when necessary.


## Making queries

Once you have a connection, there are a few things you can do with it:

* make queries with `executeQuery()`
* make streaming queries with `streamQuery()`
* run updates with `executeUpdate()`
* run whatever SQL with `execute()`
* create prepared statements with `prepareStatement()`
* control transactions with `startTransaction()`, `commit()` and `rollback()`

All the query methods have overloads that allow you to use `?` placeholders in the SQL,
where the values are then automatically inserted and escaped as appropriate. It is a 
best practice to always use the placeholders instead of manually constructing queries, 
to avoid SQL injection attacks.

```java
conn.executeQuery("SELECT * FROM logins WHERE email=?", email)
```

### Prepared statements

As mentioned above, you do not need to use prepared statements just to use `?` 
placeholders in queries, as is the case with JDBC. However, there are a few other 
reasons why they might be useful:

* they should run somewhat faster if you execute the same statement many times
* the protocol used for prepared statements is more efficient than with regular
  queries, so you may gain some performance if/when dealing with huge result sets

Still, they are not free:

* they occupy resources on both server and client
* the code becomes more complicated

## Reading results

Query results generally come in the form of a `DbResultSet`. It is a list of `DbRow`s,
together with some meta-info wrapped into a `DbColumns`, a list of `DbColumn` objects.
In turn, each `DbRow` contains `DbValue`s, which are wrappers of the underlying values
(kind of like the `Number` class can be used to read a bunch of actual numeric types, 
except we need to handle more possibilities here). You can also ignore the whole 
`DbValue` thing and just read values directly from `DbRow`:

```java
conn.executeQuery("SELECT id, first_name, last_name FROM users WHERE date_of_birth=?", LocalDate.now()).
     whenComplete((users, error) -> { // TODO: handle error
         for (DbRow user : users) {
             // these are all equivalent:
             String lastName1 = user.getString(2);
             String lastName2 = user.getString("last_name");
             String lastName3 = user.getValue(2).asString();
             String lastName4 = user.getValue("last_name").asString();
             // as are these, but they're not recommended, unless you're 100% sure what the class is
             String lastName5 = (String) user.get(2);
             String lastName6 = (String) user.getValue(2).unwrap();
         }
     });
```

### Streaming results

The query functions usually read the whole result set and store it into a `DbResultSet` object,
but if the query result is really large, it means that a lot of memory is required. If you don't
need to have the whole list of rows available at once (e.g. because you're transforming them
into domain objects, converting them into JSON or incrementally computing some statistics),
you can make the process more efficient by implementing a `DbQueryResultObserver` and using 
it with `streamQuery()`. 

## Multiple queries

There is no need to wait for one query to finish before starting the next one. If a previous
query hasn't finished executing yet, the new one will be placed into a queue and executed later.
So, it is perfectly fine to run multiple queries "at once", or to write updates like this:

```java
CompletableFuture.allOf(
    conn.startTransaction(),
    conn.executeUpdate("UPDATE some_table ..."),
    conn.executeUpdate("UPDATE other_table ..."),
    conn.executeUpdate("INSERT INTO third_table ..."),
    conn.commit()
).whenComplete((result, error) -> {
    if (error != null)
        conn.rollback();
});
```

Note that the `close()` command is also placed in the same queue, however once you call it, 
you can't do anything else using the same connection.


## A short, but complete example

The example below connects to the local database and lists all the tables visible
to the connecting user.

```java
public class ShowTables {
    public static void main(String[] args) {
        DbConfig config =
            DbConfig.newBuilder(MYSQL).
                setHost("127.0.0.1", 3356).
                setDefaultCredentials("asyncdb", "asyncdb").
                setDefaultDatabase("asyncdb").
            build();

        DbDataSource dataSource = config.makeDataSource();

        dataSource.connect().whenComplete((conn, error) -> {
            if (error != null) {
                error.printStackTrace();
                config.eventLoopGroup().shutdownGracefully();
                return;
            }

            conn.executeQuery("SELECT * FROM information_schema.tables").whenComplete((result, queryError) -> {
                if (queryError == null) {
                    System.out.println("Here are the tables present:");
                    for (DbRow row : result) {
                        String schema = row.getString("TABLE_SCHEMA");
                        String table = row.getString("TABLE_NAME");
                        int rows = row.getInt("TABLE_ROWS");
                        int avgLen = row.getInt("AVG_ROW_LENGTH");

                        System.out.println("- " + schema + "." + table + " (" + rows + " rows, average length " + avgLen + ")");
                    }
                } else {
                    queryError.printStackTrace();
                }

                conn.close().thenRun(() -> config.eventLoopGroup().shutdownGracefully());
            });
        });
    }
}
``` 


## Type mapping

When sending values to the database, the following Java types are supported:

Text:
* `java.lang.String`

Numbers:
* `java.lang.Byte`
* `java.lang.Short`
* `java.lang.Integer`
* `java.lang.Long`
* `java.lang.Float`
* `java.lang.Double`
* `java.math.BigDecimal`
* `java.math.BigInteger`
* `java.lang.Boolean` (becomes 1 or 0)
* `com.github.mslenc.asyncdb.util.ULong`

Binary values (blobs):
* `byte[]`
* `io.netty.buffer.ByteBuf`
* `java.nio.ByteBuffer`

Temporal values:
* `java.time.Duration`
* `java.time.Instant`
* `java.time.LocalDate`
* `java.time.LocalTime`
* `java.time.LocalDateTime`
* `java.time.Year`

Other:
* `com.github.mslenc.asyncdb.DbValue`
* `java.util.Optional` (containing any of the other types)

-----------------------------------

When values are read back from the database, they all become `DbValue`s, wrapping some underlying type:

### Numeric types

MySQL type  | Signed Java type  | Unsigned Java type
------------|-------------------|----------------------
`TINYINT`   | `int`             | `int`
`SMALLINT`  | `int`             | `int`
`MEDIUMINT` | `int`             | `int`
`INT`       | `int`             | `long`
`BIGINT`    | `long`            | `ULong`
`FLOAT`     | `float`           | `float`
`DOUBLE`    | `double`          | `double`
`DECIMAL`   | `BigDecimal`      | `BigDecimal`

### Textual types

MySQL type   | Java type
-------------|----------
`CHAR`       | `String`
`VARCHAR`    | `String`
`TINYTEXT`   | `String`
`TEXT`       | `String`
`MEDIUMTEXT` | `String`
`LONGTEXT`   | `String`
`ENUM`       | `String`
`SET`        | `String`

### Binary types

MySQL type   | Java type
-------------|----------
`TINYBLOB`   | `byte[]`
`BLOB`       | `byte[]`
`MEDIUMBLOB` | `byte[]`
`LONGBLOB`   | `byte[]`

### Temporal types

MySQL type   | Java type
-------------|----------
`DATE`       | `LocalDate`
`DATETIME`   | `LocalDateTime`
`TIME`       | `Duration`
`TIMESTAMP`  | `Instant`
`YEAR`       | `Year`

### Other

MySQL type   | Java type
-------------|----------
`JSON`       | `String`
`GEOMETRY`   | `byte[]`


Note that the `DbValue` allows you to read the values as other types (e.g. an `INT` as a `long`, or a `TIME` as a
`LocalTime` instead of a `Duration`), but also note that in general, the conversion is done only if it preserves the 
value, otherwise an exception is thrown. For example, you can read a `DECIMAL 125.00` as a `short`, but not 
also `125.31` (because it is not an integer number) or `95123.00` (because it is out of range for `short`).