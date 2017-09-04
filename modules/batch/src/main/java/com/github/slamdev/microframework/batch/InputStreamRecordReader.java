package com.github.slamdev.microframework.batch;

import org.easybatch.core.reader.RecordReader;
import org.easybatch.core.record.Header;
import org.easybatch.core.record.StringRecord;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;

public class InputStreamRecordReader implements RecordReader {

    private Scanner scanner;

    private long currentRecordNumber;

    private final InputStream inputStream;

    private final Charset charset;

    private final String source;

    public InputStreamRecordReader(InputStream inputStream, String source) {
        this(inputStream, UTF_8, source);
    }

    public InputStreamRecordReader(InputStream inputStream) {
        this(inputStream, UTF_8, null);
    }

    public InputStreamRecordReader(InputStream inputStream, Charset charset, String source) {
        this.inputStream = inputStream;
        this.charset = charset;
        this.source = source;
    }

    @Override
    public StringRecord readRecord() {
        Header header = new Header(++currentRecordNumber, source, new Date());
        if (scanner.hasNextLine()) {
            return new StringRecord(header, scanner.nextLine());
        } else {
            return null;
        }
    }

    @Override
    public void open() throws Exception {
        currentRecordNumber = 0;
        scanner = new Scanner(inputStream, charset.name());
    }

    @Override
    public void close() {
        if (scanner != null) {
            scanner.close();
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
