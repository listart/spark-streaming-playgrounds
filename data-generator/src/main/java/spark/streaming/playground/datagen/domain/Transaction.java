package spark.streaming.playground.datagen.domain;

import java.time.LocalDateTime;

public class Transaction {
    public long accountId;
    public int amount;
    public LocalDateTime timestamp;
}
