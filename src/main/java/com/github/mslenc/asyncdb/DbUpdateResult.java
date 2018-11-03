package com.github.mslenc.asyncdb;

import java.util.List;

public interface DbUpdateResult {
    long getRowsAffected();
    String getStatusMessage();
    List<Long> getGeneratedIds();
}
