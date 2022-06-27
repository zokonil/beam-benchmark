package hr.test.kafka;

import hr.test.constants.Constants;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;

/**
 * Kafka producer
 */
public class Producer implements Constants {

    public static void main(String[] args) {
        // Define Kafka properties
        Properties properties = new Properties();
        properties.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        properties.put("key.serializer", "org.apache.kafka.common.serialization.LongSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("auto.offset.reset", "smallest");

        // Initialize counters
        long startTime;
        long currentTime;
        long rowTimestamp;
        long lineNumber = -1L;
        long rowID = -1L;
        long numberOfIterations = 0L;

        String row; // String for reading .csv file

        try (KafkaProducer<Long, String> kafkaProducer = new KafkaProducer<>(properties)) {
            try (BufferedReader csvReader = new BufferedReader(new FileReader(INPUT_FILE_PATH))) {
                startTime = System.currentTimeMillis();
                while (numberOfIterations < ITERATIONS) {
                    numberOfIterations += 1;
                    while ((row = csvReader.readLine()) != null && lineNumber < LINES) {
                        lineNumber += 1;
                        rowID += 1;
                        if (lineNumber == 0) { // Skip header in .csv file
                            continue;
                        }
                        String kafkaProducerRecord = row + "," + System.currentTimeMillis(); // Append kafka input time
                                                                                             // to .csv row
                        currentTime = System.currentTimeMillis();
                        rowTimestamp = currentTime - startTime; // Create record timestamp as currentTime - startTime
                        kafkaProducer.send(
                                new ProducerRecord<>(INPUT_TOPIC_NAME, null, rowTimestamp, rowID, kafkaProducerRecord));
                    }
                    lineNumber = 0L; // Reset line number for each iteration
                }
            }
        } catch (Exception exc) {
            System.err.println("Error while reading input file!" + exc.getMessage());
        }
    }
}
