package com.motadata.traceorg.ipam.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;

public class TraceOrgTaskExecutor
{
    private static final ForkJoinPool m_taskExecutor = new ForkJoinPool(100, (pool) ->
    {
        ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
        worker.setName("Task Executor-" + worker.getPoolIndex());
        return worker;
    }
            , (Thread.UncaughtExceptionHandler) null, true);


    private static final ForkJoinPool m_importTaskExecutor = new ForkJoinPool(Runtime.getRuntime().availableProcessors()*2, (pool) ->
    {
        ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
        worker.setName("Task Executor-" + worker.getPoolIndex());
        return worker;
    }
            , (Thread.UncaughtExceptionHandler) null, true);


    private static final ForkJoinPool m_scanTaskExecutor = new ForkJoinPool(Runtime.getRuntime().availableProcessors()*2, (pool) ->
    {
        ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
        worker.setName("Task Executor-" + worker.getPoolIndex());
        return worker;
    }
            , (Thread.UncaughtExceptionHandler) null, true);

    public TraceOrgTaskExecutor()
    {

    }

    public static ForkJoinTask<?> executeTask(Callable<?> task)
    {
        return m_taskExecutor.submit(task);
    }

    public static ForkJoinTask<?> executeImportCSVTask(Callable<?> task)
    {
        return m_importTaskExecutor.submit(task);
    }

    public static ForkJoinTask<?> executeScanTask(Callable<?> task)
    {
        return m_scanTaskExecutor.submit(task);
    }

    public static ForkJoinPool getTaskExecutor()
    {
        return m_taskExecutor;
    }

    public static ForkJoinPool getScanTaskExecutor()
    {
        return m_scanTaskExecutor;
    }

    public static ForkJoinPool getInsertTaskExecutor()
    {
        return m_importTaskExecutor;
    }
}
