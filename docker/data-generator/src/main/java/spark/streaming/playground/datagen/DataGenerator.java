package spark.streaming.playground.datagen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataGenerator {
    public static final Logger LOG = LoggerFactory.getLogger(DataGenerator.class);
    public static final String KAFKA = "kafka:9092";
    public static final String TOPIC = "transactions";

    public static void main(String[] args) {
        Producer producer = new Producer(KAFKA, TOPIC);

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    LOG.info("Shutting down");
                    producer.close();
                })
        );

        producer.run();
    }
}
