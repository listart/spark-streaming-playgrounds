package spark.streaming.playground.datagen;

final class Throttler {
    private final long throttleBatchSize;
    private final long nanoPerBatch;

    private long endOfNextBatchNanos;
    private int currentBatch;

    Throttler(long maxRecordsPerSecond) {
        if (maxRecordsPerSecond == -1) {
            // unlimited speed
            throttleBatchSize = -1;
            nanoPerBatch = 0;
        } else {
            final float ratePerSubtask = (float) maxRecordsPerSecond;
            if (ratePerSubtask >= 10000) {
                throttleBatchSize = (int) ratePerSubtask / 500;
                nanoPerBatch = 2_000_000L;
            } else {
                throttleBatchSize = ((int) (ratePerSubtask / 20)) + 1;
                nanoPerBatch = ((int) (1_000_000_000L / ratePerSubtask)) * throttleBatchSize;
            }
        }

        this.endOfNextBatchNanos = System.nanoTime() + nanoPerBatch;
        this.currentBatch = 0;
    }

    void throttle() throws  InterruptedException {
        if (throttleBatchSize == -1) return;
        if (++currentBatch != throttleBatchSize) return;

        currentBatch = 0;

        final long now = System.nanoTime();
        final int millisRemaining = (int) ((endOfNextBatchNanos - now) / 1_000_000);

        if (millisRemaining > 0) {
            endOfNextBatchNanos += nanoPerBatch;
            Thread.sleep(millisRemaining);
        } else {
            endOfNextBatchNanos = now + nanoPerBatch;
        }
    }
}
