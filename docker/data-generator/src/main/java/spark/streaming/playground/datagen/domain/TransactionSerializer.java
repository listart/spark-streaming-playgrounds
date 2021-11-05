package spark.streaming.playground.datagen.domain;

import org.apache.kafka.common.serialization.Serializer;

import java.time.format.DateTimeFormatter;
import java.util.Map;

public class TransactionSerializer implements Serializer<Transaction> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void configure(Map<String, ?> map, boolean b) {
    }

    @Override
    public byte[] serialize(String s, Transaction transaction) {
        String csv = String.format(
                "%s,%s,%s",
                transaction.accountId,
                transaction.amount,
                transaction.timestamp.format(formatter)
        );

        return csv.getBytes();
    }

    @Override
    public void close() {
    }
}
