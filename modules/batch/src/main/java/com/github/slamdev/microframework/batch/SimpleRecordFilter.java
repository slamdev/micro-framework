package com.github.slamdev.microframework.batch;

import org.easybatch.core.filter.RecordFilter;
import org.easybatch.core.record.Record;

import java.util.function.Predicate;

public class SimpleRecordFilter<T> implements RecordFilter<Record<Object>> {

    private final Predicate<T> filter;

    public SimpleRecordFilter(Predicate<T> filter) {
        this.filter = filter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Record<Object> processRecord(Record record) {
        return filter.test((T) record.getPayload()) ? record : null;
    }
}
