package com.github.mslenc.asyncdb.common.general;

import com.github.mslenc.asyncdb.common.ResultSet;
import com.github.mslenc.asyncdb.common.RowData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MutableResultSet<T extends ColumnData> extends AbstractList<RowData> implements ResultSet {
    private static final Logger log = LoggerFactory.getLogger(MutableResultSet.class);

    public final List<T> columnTypes;
    private final ArrayList<RowData> rows = new ArrayList<>();
    private final Map<String, Integer> columnMapping = new HashMap<>();
    private final List<String> columnNames = new ArrayList<>();
    private final List<Integer> types = new ArrayList<>();

    public MutableResultSet(List<T> columnTypes) {
        this.columnTypes = columnTypes;

        for (int a = 0; a < columnTypes.size(); a++) {
            T col = columnTypes.get(a);
            columnNames.add(col.name());
            columnMapping.put(col.name(), a);
            types.add(col.dataType());
        }
    }

    public void addRow(Object[] values) {
        this.rows.add(new ArrayRowData(this.rows.size(), columnMapping, values));
    }

    @Override
    public RowData get(int index) {
        return rows.get(index);
    }

    @Override
    public int size() {
        return rows.size();
    }

    @Override
    public List<String> getColumnNames() {
        return columnNames;
    }

    public int getNumColumns() {
        return columnTypes.size();
    }
}
