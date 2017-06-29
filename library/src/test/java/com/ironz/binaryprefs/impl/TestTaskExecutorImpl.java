package com.ironz.binaryprefs.impl;

import com.ironz.binaryprefs.task.Completable;
import com.ironz.binaryprefs.task.TaskExecutor;

import java.util.concurrent.*;

public class TestTaskExecutorImpl implements TaskExecutor {

    private static final ExecutorService e = SameThreadExecutorService.getInstance();

    @Override
    public Completable submit(Runnable runnable) {
        Future<?> submit = e.submit(runnable);
        return new Completable(submit);
    }

    private static final class SameThreadExecutorService extends ThreadPoolExecutor {

        private final CountDownLatch signal = new CountDownLatch(1);

        private SameThreadExecutorService() {
            super(1, 1, 0, TimeUnit.DAYS, new SynchronousQueue<Runnable>(),
                    new ThreadPoolExecutor.CallerRunsPolicy());
        }

        @Override
        public void shutdown() {
            super.shutdown();
            signal.countDown();
        }

        static ExecutorService getInstance() {
            return SingletonHolder.instance;
        }

        private static class SingletonHolder {
            static ExecutorService instance = createInstance();
        }

        private static ExecutorService createInstance() {

            final SameThreadExecutorService instance = new SameThreadExecutorService();

            // The executor has one worker thread. Give it a Runnable that waits
            // until the executor service is shut down.
            instance.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        instance.signal.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            return Executors.unconfigurableExecutorService(instance);
        }
    }
}