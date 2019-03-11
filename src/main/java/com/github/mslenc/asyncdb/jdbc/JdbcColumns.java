package com.github.mslenc.asyncdb.jdbc;

import com.github.mslenc.asyncdb.DbColumn;
import com.github.mslenc.asyncdb.DbColumns;
import com.github.mslenc.asyncdb.DbRow;
import com.github.mslenc.asyncdb.DbValue;
import com.github.mslenc.asyncdb.impl.DbRowImpl;
import com.github.mslenc.asyncdb.jdbc.val.*;

import java.sql.ParameterMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.AbstractList;
import java.util.HashMap;

import static java.sql.Types.*;

public class JdbcColumns extends AbstractList<DbColumn> implements DbColumns {
    private final JdbcColumn[] columns;
    private HashMap<String, JdbcColumn> indexByName;

    public JdbcColumns(JdbcColumn[] columns) {
        this.columns = columns;
    }

    public JdbcColumn get(int index) {
        return columns[index];
    }

    public JdbcColumn get(String columnName) {
        HashMap<String, JdbcColumn> indexByName = this.indexByName;
        if (indexByName == null) {
            indexByName = new HashMap<>();
            for (JdbcColumn columnDef : columns) {
                indexByName.put(columnDef.getName(), columnDef);
            }
            this.indexByName = indexByName;
        }
        return indexByName.get(columnName);
    }

    @Override
    public int size() {
        return columns.length;
    }

    public DbRow extractRow(ResultSet rs, int rowIndex) throws SQLException {
        int numCols = columns.length;
        DbValue[] values = new DbValue[numCols];
        for (int i = 0; i < numCols; i++)
            values[i] = columns[i].extract(rs);

        return new DbRowImpl(rowIndex, values, this);
    }

    public static JdbcColumns extractColumns(ResultSetMetaData metaData) throws SQLException {
        int numCols = metaData.getColumnCount();
        JdbcColumn[] columns = new JdbcColumn[numCols];

        for (int i = 1; i <= numCols; i++) {
            String name = metaData.getColumnName(i);
            JdbcGetter getter = getterForSqlType(metaData.getColumnType(i), metaData.isSigned(i));

            columns[i - 1] = new JdbcColumn(name, i - 1, getter);
        }

        return new JdbcColumns(columns);
    }

    public static JdbcColumns extractColumns(ParameterMetaData metaData) throws SQLException {
        // TODO: this should probably be something like JdbcParameters (implementing DbParameters)
        // TODO: JDBC setters in addition to getters
        int numCols = metaData.getParameterCount();
        JdbcColumn[] columns = new JdbcColumn[numCols];

        for (int i = 1; i <= numCols; i++) {
            String name = metaData.getParameterTypeName(i);
            JdbcGetter getter = getterForSqlType(metaData.getParameterType(i), metaData.isSigned(i));

            columns[i - 1] = new JdbcColumn(name, i - 1, getter);
        }

        return new JdbcColumns(columns);
    }

    static JdbcGetter getterForSqlType(int columnType, boolean isSigned) throws SQLException {
        switch (columnType) {
            case TINYINT:
            case SMALLINT:
                return JdbcGetterInt.instance();

            case INTEGER:
                if (isSigned) {
                    return JdbcGetterInt.instance();
                } else {
                    return JdbcGetterLong.instance();
                }

            case BIGINT:
                if (isSigned) {
                    return JdbcGetterLong.instance();
                } else {
                    return JdbcGetterULong.instance();
                }

            case REAL:
                return JdbcGetterFloat.instance();

            case DOUBLE:
            case FLOAT:
                return JdbcGetterDouble.instance();

            case DECIMAL:
            case NUMERIC:
                return JdbcGetterBigDecimal.instance();

            case CHAR:
            case VARCHAR:
            case NCHAR:
            case NVARCHAR:
                return JdbcGetterString.instance();

            case LONGVARCHAR:
            case LONGNVARCHAR:
            case CLOB:
            case NCLOB:
                return JdbcGetterClob.instance();

            case BINARY:
            case VARBINARY:
                return JdbcGetterBytes.instance();

            case LONGVARBINARY:
            case BLOB:
                return JdbcGetterBlob.instance();

            case DATE:
                return JdbcGetterDate.instance();

            case TIME:
                return JdbcGetterTime.instance();

            case TIMESTAMP:
                return JdbcGetterTimestamp.instance();

            case TIME_WITH_TIMEZONE:
                return JdbcGetterTZTime.instance();

            case TIMESTAMP_WITH_TIMEZONE:
                return JdbcGetterTZDateTime.instance();

            case BIT:
            case BOOLEAN:
                return JdbcGetterBoolean.instance();

            case NULL:
                return JdbcGetterNull.instance();

            case OTHER:
            case JAVA_OBJECT:
            case DISTINCT:
            case STRUCT:
            case ARRAY:
            case REF:
            case DATALINK:
            case ROWID:
            case SQLXML:
            case REF_CURSOR:
            default:
                throw new SQLException("Unsupported SQL type " + columnType);
        }
    }
}
