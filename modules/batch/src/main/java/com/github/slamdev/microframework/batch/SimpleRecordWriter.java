package com.github.slamdev.microframework.batch;

import org.easybatch.core.record.Batch;
import org.easybatch.core.writer.RecordWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SimpleRecordWriter<T> implements RecordWriter {

    private final Consumer<List<T>> writer;

    public SimpleRecordWriter(Consumer<List<T>> writer) {
        this.writer = writer;
    }

    @Override
    public void open() {
        // no-op
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writeRecords(Batch batch) {
        List<T> records = new ArrayList<T>((int) batch.size());
        batch.iterator().forEachRemaining(record -> records.add((T) record.getPayload()));
        writer.accept(records);
    }

    @Override
    public void close() {
        // no-op
    }
}
