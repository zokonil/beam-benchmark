package hr.test.kafka;

import hr.test.constants.Constants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Kafka consumer
 */
public class Consumer implements Constants {

    public static void main(String[] args) {
        // Define Kafka properties
        Properties properties = new Properties();
        properties.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.LongDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("group.id", "consumer-group");
        KafkaConsumer<Long, String> kafkaConsumer = new KafkaConsumer<>(properties);
        List<String> topics = new ArrayList<>();
        topics.add(OUTPUT_TOPIC_NAME);
        kafkaConsumer.subscribe(topics);

        // Initialize counters
        long processingTimeDuration;
        long kafkaTimeDuration;
        long kafkaOutputTime;
        long kafkaInputTime = -1L;
        long processingTimeStart = -1L;
        long processingTimeEnd = -1L;
        long numberOfRecords = 0L;

        System.out.println("hello im started");

        String result = ""; // Kafka record value

        try {
            while (true) {
                ConsumerRecords<Long, String> records = kafkaConsumer.poll(CONSUMER_POLL_MS);
                for (ConsumerRecord<Long, String> record : records) {
                    kafkaOutputTime = System.currentTimeMillis();
                    String recordValue = record.value();
                    String[] entry = recordValue.split(","); // Split value with "," delimiter
                    for (String data : entry) {
                        if (data.trim().startsWith("OperationResult")) { // Task result
                            result = data.trim().replace("'", "").split("=", -1)[1];
                        } else if (data.trim().startsWith(NUMBER_OF_RECORDS)) { // Number of records
                            numberOfRecords = Long.parseLong(data.trim().split("=", -1)[1]);
                        } else if (data.trim().startsWith(PROCESSING_START_TIME_NAME)) { // Processing time start
                            processingTimeStart = Long.parseLong(data.trim().split("=", -1)[1]);
                        } else if (data.trim().startsWith(PROCESSING_END_TIME_NAME)) { // Processing time end
                            processingTimeEnd = Long.parseLong(data.trim().split("=", -1)[1]);
                        } else if (data.trim().startsWith(KAFKA_START_TIME_NAME)) { // Kafka input time
                            kafkaInputTime = Long.parseLong(data.replace("}", "").trim().split("=", -1)[1]);
                        }
                    }
                    // Calculate durations
                    processingTimeDuration = processingTimeEnd - processingTimeStart;
                    kafkaTimeDuration = kafkaOutputTime - kafkaInputTime;
                    // Print results
                    System.out.println("Results: " + "\n" + result);
                    System.out.println("Processing time duration= "
                            + String.format("%.2f", processingTimeDuration / 1000.00)
                            + " s, Row count= " + numberOfRecords
                            + ", Processing throughput= "
                            + String.format("%.2f", numberOfRecords / (processingTimeDuration / 1000.00)) + " row/s");
                    System.out.println("Kafka time duration= " + String.format("%.2f", kafkaTimeDuration / 1000.00)
                            + " s, Row count= " + numberOfRecords
                            + ", Kafka throughput= "
                            + String.format("%.2f", numberOfRecords / (kafkaTimeDuration / 1000.00)) + " row/s\n");
                }
            }
        } catch (Exception exc) {
            System.err.println("Error while reading from output topic! " + exc.getMessage());
        } finally {
            kafkaConsumer.close();
        }
    }

}