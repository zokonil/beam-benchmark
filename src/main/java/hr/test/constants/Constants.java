package hr.test.constants;

/**
 * Interface for constants
 * Note: If you change any constant, you need to recompile your program and
 * restart Kafka consumer/producer
 */
public interface Constants {
  String INPUT_FILE_PATH = "/home/ubuntu/diplomski-new/realData.csv"; // Path to input .csv file
  long LINES = 1000000L; // Number of lines to read
  long ITERATIONS = 1L; // Number of iterations for reading input .csv file
  float PERCENTAGE = (float) 0.95; // Percentage of messages that pipeline has to read from Kafka producer (value
                                   // must be less or equal to 1)
  long CONSUMER_POLL_MS = 5000; // Consumer poll time in [ms]
  String BOOTSTRAP_SERVERS = "localhost:9092"; // Bootstrap server URL
  String INPUT_TOPIC_NAME = "input"; // Name of Kafka input topic
  String OUTPUT_TOPIC_NAME = "output"; // Name of Kafka output topic
  String KAFKA_START_TIME_NAME = "kafkaInputTime"; // Name of parameter "kafkaInputTime"
  String PROCESSING_START_TIME_NAME = "processingTimeStart"; // Name of parameter "processingTimeStart"
  String PROCESSING_END_TIME_NAME = "processingEndTime"; // Name of parameter "processingEndTime"
  String NUMBER_OF_RECORDS = "numberOfRecords"; // Name of parameter "numberOfRecords"

  int NUM_OF_KAFKA_PARTITIONS = 2;
  int N = 10; // For task "Top N"
  String DATE = "05/12/2017"; // For task "Composite transformation"
  long DATA_STREAM_ID = 33305; // For task "Filtration"
}
