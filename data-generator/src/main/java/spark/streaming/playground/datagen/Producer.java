package spark.streaming.playground.datagen;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import spark.streaming.playground.datagen.domain.Transaction;
import spark.streaming.playground.datagen.domain.TransactionSerializer;
import spark.streaming.playground.datagen.domain.TransactionSupplier;

import java.time.ZoneOffset;
import java.util.Properties;

public class Producer implements Runnable, AutoCloseable {
    private final String brokers;
    private final String topic;
    private volatile boolean isRunning;

    public Producer(String brokers, String topic) {
        this.brokers = brokers;
        this.topic = topic;
        this.isRunning = true;
    }

    private Properties getProperties() {
        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, TransactionSerializer.class);

        return props;
    }

    @Override
    public void close() {
        isRunning = false;
    }

    @Override
    public void run() {
        KafkaProducer<Long, Transaction> producer = new KafkaProducer<>(getProperties());

        Throttler throttler = new Throttler(100);

        TransactionSupplier transactions = new TransactionSupplier();

        while (isRunning) {
            Transaction transaction = transactions.get();

            long millis = transaction.timestamp.atZone(ZoneOffset.UTC).toInstant().toEpochMilli();

            ProducerRecord<Long, Transaction> record =
                    new ProducerRecord<>(topic, null, millis, transaction.accountId, transaction);
            producer.send(record);

            try {
                throttler.throttle();
            } catch (InterruptedException e) {
                isRunning = false;
            }
        }

        producer.close();
    }
}
