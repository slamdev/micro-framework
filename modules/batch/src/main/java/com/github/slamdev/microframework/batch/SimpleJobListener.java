package com.github.slamdev.microframework.batch;

import org.easybatch.core.job.JobParameters;
import org.easybatch.core.job.JobReport;
import org.easybatch.core.listener.JobListener;

import java.util.function.Consumer;

public class SimpleJobListener implements JobListener {

    private final Consumer<JobParameters> beforeJobStartConsumer;

    private final Consumer<JobReport> afterJobEndConsumer;

    public SimpleJobListener(Consumer<JobReport> afterJobEndConsumer) {
        this(jobParameters -> {
        }, afterJobEndConsumer);
    }

    public SimpleJobListener(Consumer<JobParameters> beforeJobStartConsumer, Consumer<JobReport> afterJobEndConsumer) {
        this.beforeJobStartConsumer = beforeJobStartConsumer;
        this.afterJobEndConsumer = afterJobEndConsumer;
    }

    @Override
    public void beforeJobStart(JobParameters jobParameters) {
        beforeJobStartConsumer.accept(jobParameters);
    }

    @Override
    public void afterJobEnd(JobReport jobReport) {
        afterJobEndConsumer.accept(jobReport);
    }
}
