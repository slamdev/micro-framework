package com.github.slamdev.microframework.batch;

import org.easybatch.core.reader.RecordReader;
import org.easybatch.core.record.Record;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class TimedBlockingQueueRecordReader implements RecordReader {

    private final BlockingQueue<Record> queue;

    private final long timeout;

    private final TimeUnit unit;

    public TimedBlockingQueueRecordReader(BlockingQueue<Record> queue, long timeout, TimeUnit unit) {
        this.queue = queue;
        this.timeout = timeout;
        this.unit = unit;
    }

    @Override
    public void open() {
    }

    @Override
    public Record readRecord() throws Exception {
        return queue.poll(timeout, unit);
    }

    @Override
    public void close() {
        // no op
    }
}
