package com.github.mslenc.asyncdb.common;

import java.util.List;

public interface ResultSet extends List<RowData> {
    List<String> getColumnNames();
}
