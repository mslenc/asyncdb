# asyncdb
Asynchronous (MySQL) database client for Java

Work in progress, more README coming soon.


## Type mapping

### Numeric types

MySQL type | Java type if signed | Java type if unsigned
-----------|---------------------|----------------------
TINYINT    | Byte                | Short
SMALLINT   | Short               | Integer
MEDIUMINT  | Integer             | Integer
INT        | Integer             | Long
BIGINT     | Long                | ULong
FLOAT      | Float               | Float
DOUBLE     | Double              | Double
DECIMAL    | BigDecimal          | BigDecimal

### Textual types

MySQL type | Java type
-----------|----------
CHAR       | String
VARCHAR    | String
TINYTEXT   | String
TEXT       | String
MEDIUMTEXT | String
LONGTEXT   | String
ENUM       | String
SET        | String

### Binary types

MySQL type | Java type
-----------|----------
TINYBLOB   | byte[]
BLOB       | byte[]
MEDIUMBLOB | byte[]
LONGBLOB   | byte[]

### Temporal types

MySQL type | Java type
-----------|----------
DATE       | LocalDate
DATETIME   | LocalDateTime
TIME       | Duration
TIMESTAMP  | Instant
YEAR       | Year
