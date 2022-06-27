package hr.test.beam;

import hr.test.constants.Constants;
import hr.test.entity.Accum;
import hr.test.entity.OperationResult;
import hr.test.entity.SensorReading;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.io.kafka.KafkaRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.beam.sdk.io.TextIO;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * Main class for executing pipeline
 */

public class Beam implements Constants {

  /**
   * Custom defined pipeline options.
   */
  public interface BeamOptions extends PipelineOptions {
    @Description("Task to execute")
    String getTask();

    void setTask(String task); // Setter must also be defined!
  }

  // ****************************************** HELPER CLASSES
  // ********************************************************

  /**
   * Helper static classes for various transformation:
   * - OperationResultFn
   * - TransformLongToKVDoFn
   * - TransformKVLongToKVDoubleDoFn
   * - TransformListToKVDoFn
   * - TransformStartTimeToKVDoFn
   * - ExtractStartTimeDoFn
   * - ExtractMinStartTime
   **/

  /**
   * Class that transforms KV<String, Double> to OperationResult.
   * This class maps data from key-value to single record that represents result.
   */
  public static class OperationResultFn extends Combine.CombineFn<KV<String, Double>, Accum, OperationResult> {

    /**
     * Method that creates accumulator.
     * 
     * @return accumulator
     */
    @Override
    public Accum createAccumulator() {
      return new Accum();
    }

    /**
     * Method that maps single KV value to accumulator value.
     * 
     * @param accum accumulator
     * @param input KV pair
     * @return accumulator
     */
    @Override
    public Accum addInput(Accum accum, KV<String, Double> input) {
      String key = input.getKey();
      double value = input.getValue();

      if (key.equals(Constants.PROCESSING_START_TIME_NAME)) {
        accum.processingTimeStart = value;
      } else if (key.equals(Constants.KAFKA_START_TIME_NAME)) {
        accum.kafkaInputTime = value;
      } else {
        accum.result += key + ": " + value + "\n";
      }
      return accum;
    }

    /**
     * Method that merges multiple accumulators
     * 
     * @param accums accumulators
     * @return single accumulator
     */
    @Override
    public Accum mergeAccumulators(Iterable<Accum> accums) {
      Accum merged = createAccumulator();
      for (Accum accum : accums) {
        merged.result += accum.result;
        merged.processingTimeStart += accum.processingTimeStart;
        merged.kafkaInputTime += accum.kafkaInputTime;
      }
      return merged;
    }

    /**
     * Method that extracts value from accumulator and transforms is into output
     * result
     * 
     * @param accum accumulator
     * @return OperationResult
     */
    @Override
    public OperationResult extractOutput(Accum accum) {
      return new OperationResult(accum.result,
          (long) (Constants.LINES * Constants.ITERATIONS * Constants.PERCENTAGE),
          (long) accum.processingTimeStart,
          -1L,
          (long) accum.kafkaInputTime);
    }
  }

  /**
   * Transforms Long to key-value.
   */
  public static class TransformLongToKVDoFn extends DoFn<Long, KV<String, Double>> {
    String key; // Key for KV

    public TransformLongToKVDoFn(String key) {
      this.key = key;
    }

    @ProcessElement
    public void processElement(@Element Long result, OutputReceiver<KV<String, Double>> receiver) {
      receiver.output(KV.of(this.key, (double) result));
    }
  }

  /**
   * Transforms KV<String, Long> to KV<String, Double>.
   */
  public static class TransformKVLongToKVDoubleDoFn extends DoFn<KV<String, Long>, KV<String, Double>> {
    @ProcessElement
    public void processElement(@Element KV<String, Long> element, OutputReceiver<KV<String, Double>> receiver) {
      receiver.output(KV.of(element.getKey(), (double) element.getValue()));
    }
  }

  /**
   * Transforms List<Double> to KV<String, Double> where key is index + 1 of
   * element in list
   */
  public static class TransformListToKVDoFn extends DoFn<List<Double>, KV<String, Double>> {
    @ProcessElement
    public void processElement(@Element List<Double> inputList, OutputReceiver<KV<String, Double>> receiver) {
      for (int i = 0; i < inputList.size(); i++) {
        receiver.output(KV.of((i + 1) + ". ", inputList.get(i)));
      }
    }
  }

  /**
   * Transforms start time (double) to KV<String, Double> where key is input
   * parameter startTimeName.
   */
  public static class TransformStartTimeToKVDoFn extends DoFn<Double, KV<String, Double>> {
    String startTimeName; // Key for KV

    public TransformStartTimeToKVDoFn(String startTimeName) {
      this.startTimeName = startTimeName;
    }

    @ProcessElement
    public void processElement(@Element Double element, OutputReceiver<KV<String, Double>> receiver) {
      receiver.output(KV.of(this.startTimeName, element));
    }
  }

  /**
   * Extracts start time from sensor reading.
   * Can extract processing start time or kafka input time (depends on input
   * parameter startTimeName)
   */
  public static class ExtractStartTimeDoFn extends DoFn<SensorReading, Double> {
    String startTimeName; // Name of time for extraction

    public ExtractStartTimeDoFn(String startTimeName) {
      this.startTimeName = startTimeName;
    }

    @ProcessElement
    public void processElement(@Element SensorReading element, OutputReceiver<Double> receiver) {
      if (startTimeName.equals(Constants.KAFKA_START_TIME_NAME)) {
        if (element.getKafkaInputTime() != -1L) {
          receiver.output((double) element.getKafkaInputTime());
        }
      } else if (startTimeName.equals(Constants.PROCESSING_START_TIME_NAME)) {
        if (element.getProcessingTimeStart() != -1L) {
          receiver.output((double) element.getProcessingTimeStart());
        }
      }
    }
  }

  /**
   * Extracts minimal start time from sensor reading records.
   */
  public static class ExtractMinimalStartTime
      extends PTransform<PCollection<SensorReading>, PCollection<KV<String, Double>>> {
    String startTimeName; // Name of time for extraction

    public ExtractMinimalStartTime(String startTimeName) {
      this.startTimeName = startTimeName;
    }

    @Override
    public PCollection<KV<String, Double>> expand(PCollection<SensorReading> inputSensorReadings) {
      PCollection<Double> extractStartTime = inputSensorReadings
          .apply("Extract start time from sensor readings", ParDo.of(new ExtractStartTimeDoFn(this.startTimeName)));
      PCollection<Double> minimalStartTime = extractStartTime
          .apply("Calculate minimal value", Min.doublesGlobally());
      return minimalStartTime.apply("Transform minimal start time to key-value",
          ParDo.of(new TransformStartTimeToKVDoFn(this.startTimeName)));
    }
  }

  // ******************************************************************************************************************

  // ****************************************** 1) TRANSFORM TO SENSOR DATA
  // *******************************************

  /**
   * Transforms input data to sensor reading.
   */
  public static class TransformInputDataToSensorReading
      extends SimpleFunction<KafkaRecord<Long, String>, SensorReading> {
    @Override
    public SensorReading apply(KafkaRecord<Long, String> input) {
      String[] data = input.getKV().getValue().split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"); // Data has "," as delimiter


      //for(int i =0; i<data.length; i++) {
    //	System.out.println(i +".: "+data[i]);
      //}
      //System.out.println("==============================");
      // Long data type
      long dataStreamID = data[9].isEmpty() ? -1L : Long.parseLong(data[9]);
      long resourceID = data[10].isEmpty() ? -1L : Long.parseLong(data[10]);

      // Time data
      long processingTimeStart = System.currentTimeMillis();
      long kafkaInputTime = data[16].isEmpty() ? -1L : Long.parseLong(data[16]);

      // Create new object of class SensorReading
      return new SensorReading(input.getKV().getKey(), data[0], data[1], data[2], data[3], data[4], data[5],
          data[6], data[7], data[8], dataStreamID, resourceID, data[11], data[12], data[13], data[14],
          data[15], processingTimeStart, -1L, kafkaInputTime, true);
    }
  }
  // ******************************************************************************************************************

  // ******************************************* 2a) AGGREGATION
  // ******************************************************

  /**
   * Helper class for extracting measurement title from sensor data.
   */
  public static class ExtractMeasurementTitleDoFn extends DoFn<SensorReading, String> {
    @ProcessElement
    public void processElement(@Element SensorReading element, OutputReceiver<String> receiver) {
      if (!element.getMeasurementTitle().isEmpty()) { // Output element if he isn't blank
        receiver.output(element.getMeasurementTitle());
      }
    }
  }

  /**
   * Returns number of records for each measurement title.
   */
  public static class TaskAggregation extends PTransform<PCollection<SensorReading>, PCollection<OperationResult>> {
    @Override
    public PCollection<OperationResult> expand(PCollection<SensorReading> inputSensorReadings) {
	System.out.println("Starting processing");
      PCollection<String> measurementTitles = inputSensorReadings
          .apply("Extract measurement title from sensor data", ParDo.of(new ExtractMeasurementTitleDoFn()));
      PCollection<KV<String, Long>> numberOfMeasurementTitles = measurementTitles
          .apply("Count number of rows for each measurement title", Count.perElement());
      PCollection<KV<String, Double>> numberOfMeasurementTitlesDouble = numberOfMeasurementTitles
          .apply("Transform long to double", ParDo.of(new TransformKVLongToKVDoubleDoFn()));

      PCollection<KV<String, Double>> extractProcessingStartTime = inputSensorReadings
          .apply("Extract minimal processing start time",
              new ExtractMinimalStartTime(Constants.PROCESSING_START_TIME_NAME));
      PCollection<KV<String, Double>> extractKafkaInputTime = inputSensorReadings
          .apply("Extract minimal kafka input time", new ExtractMinimalStartTime(Constants.KAFKA_START_TIME_NAME));

      PCollection<KV<String, Double>> flattenCollection = PCollectionList
          .of(extractProcessingStartTime)
          .and(extractKafkaInputTime)
          .and(numberOfMeasurementTitlesDouble)
          .apply("Flatten multiple collections to single PCollection", Flatten.<KV<String, Double>>pCollections());

      return flattenCollection.apply("Generate operation result", Combine.globally((new OperationResultFn())));
    }
  }
  // ******************************************************************************************************************

  // ******************************************* 2b) SPECIFIC VALUES
  // **************************************************

  /**
   * Helper class for extracting measurement title and units from sensor reading.
   */
  public static class ExtractMeasurementTitleAndUnitsDoFn extends DoFn<SensorReading, KV<String, Double>> {

    String measurementTag; // Tag for specific value

    public ExtractMeasurementTitleAndUnitsDoFn(String measurementTag) {
      this.measurementTag = measurementTag;
    }

    @ProcessElement
    public void processElement(@Element SensorReading element, OutputReceiver<KV<String, Double>> receiver) {
      if (!element.getMeasurementTitle().isEmpty()) {
        if (!element.getUnits().isEmpty() && !element.getUnitsAbbreviation().isEmpty()
            && !element.getMeasurementValue().isEmpty()) {
          String key = element.getMeasurementTitle() + " " + this.measurementTag
              + " value" + " Units: " + element.getUnits() + " (" + element.getUnitsAbbreviation() + ")";
          Double value = Double.parseDouble(element.getMeasurementValue().trim().replace(",", "."));
          receiver.output(KV.of(key, value));
        }
      }
    }
  }

  /**
   * Specific values:
   * - Minimal
   * - Average
   * - Maximal
   *
   * Returns specific values for every sensor reading.
   * Extracts measurement title and measurement unit and computes specific values
   * for them.
   */

  public static class TaskSpecificValues extends PTransform<PCollection<SensorReading>, PCollection<OperationResult>> {
    @Override
    public PCollection<OperationResult> expand(PCollection<SensorReading> inputSensorReadings) {
      PCollection<KV<String, Double>> minimalMeasurementTitlesAndValues = inputSensorReadings
          .apply("PCollection for minimal value", ParDo.of(new ExtractMeasurementTitleAndUnitsDoFn("Minimal")));
      PCollection<KV<String, Double>> averageMeasurementTitlesAndValues = inputSensorReadings
          .apply("PCollection for average value", ParDo.of(new ExtractMeasurementTitleAndUnitsDoFn("Average")));
      PCollection<KV<String, Double>> maximalMeasurementTitlesAndValues = inputSensorReadings
          .apply("PCollection for maximal value", ParDo.of(new ExtractMeasurementTitleAndUnitsDoFn("Maximal")));
      PCollection<KV<String, Double>> minimalValue = minimalMeasurementTitlesAndValues
          .apply("Calculate minimal value per key", Min.perKey());
      PCollection<KV<String, Double>> averageValue = averageMeasurementTitlesAndValues
          .apply("Calculate average value per key", Mean.perKey());
      PCollection<KV<String, Double>> maximalValue = maximalMeasurementTitlesAndValues
          .apply("Calculate maximal value per key", Max.perKey());

      PCollection<KV<String, Double>> extractProcessingStartTime = inputSensorReadings
          .apply("Extract minimal processing start time",
              new ExtractMinimalStartTime(Constants.PROCESSING_START_TIME_NAME));
      PCollection<KV<String, Double>> extractKafkaInputTime = inputSensorReadings
          .apply("Extract minimal kafka input time", new ExtractMinimalStartTime(Constants.KAFKA_START_TIME_NAME));

      PCollection<KV<String, Double>> flattenCollection = PCollectionList
          .of(extractProcessingStartTime)
          .and(extractKafkaInputTime)
          .and(minimalValue)
          .and(averageValue)
          .and(maximalValue)
          .apply("Flatten multiple collections to single PCollection", Flatten.<KV<String, Double>>pCollections());

      return flattenCollection.apply("Generate operation result", Combine.globally((new OperationResultFn())));
    }
  }
  // ******************************************************************************************************************

  // ******************************************* 2c) TOP N
  // ************************************************************

  /**
   * Helper class for extracting measurement value from sensor reading.
   */
  public static class ExtractMeasurementValueDoFn extends DoFn<SensorReading, Double> {
    @ProcessElement
    public void processElement(@Element SensorReading element, OutputReceiver<Double> receiver) {
      if (!element.getMeasurementValue().isEmpty()) {
        receiver.output(Double.parseDouble(element.getMeasurementValue().trim().replace(",", ".")));
      }
    }
  }

  /**
   * Returns top N measurement values where parameter "N" is defined in interface
   * "Constants"
   */
  public static class TaskTopN extends PTransform<PCollection<SensorReading>, PCollection<OperationResult>> {
    @Override
    public PCollection<OperationResult> expand(PCollection<SensorReading> inputSensorReadings) {
      PCollection<Double> measurementValues = inputSensorReadings
          .apply("Extract measurement values", ParDo.of(new ExtractMeasurementValueDoFn()));
      PCollection<List<Double>> listResultsTopN = measurementValues
          .apply("Calculate top N results", Top.largest(N));
      PCollection<KV<String, Double>> extractTopN = listResultsTopN.apply(
          "Extract top N results and transform list to key-value PCollection", ParDo.of(new TransformListToKVDoFn()));

      PCollection<KV<String, Double>> extractProcessingStartTime = inputSensorReadings
          .apply("Extract minimal processing start time",
              new ExtractMinimalStartTime(Constants.PROCESSING_START_TIME_NAME));
      PCollection<KV<String, Double>> extractKafkaInputTime = inputSensorReadings
          .apply("Extract minimal kafka input time", new ExtractMinimalStartTime(Constants.KAFKA_START_TIME_NAME));

      PCollection<KV<String, Double>> flattenCollection = PCollectionList
          .of(extractProcessingStartTime)
          .and(extractKafkaInputTime)
          .and(extractTopN)
          .apply("Flatten multiple collections to single PCollection", Flatten.<KV<String, Double>>pCollections());

      return flattenCollection.apply("Generate operation result", Combine.globally((new OperationResultFn())));
    }
  }
  // ******************************************************************************************************************

  // ******************************************* 2d) COMPOSITE TRANSFORMATION
  // *****************************************
  /**
   * Helper class for filtering sensor reading that has measurementTime property
   * equal to date defined in interface "Constants".
   */
  public static class FilterSensorReadingDateDoFn extends DoFn<SensorReading, SensorReading> {
    @ProcessElement
    public void processElement(@Element SensorReading element, OutputReceiver<SensorReading> receiver) {
      if (!element.getMeasurementTime().isEmpty() && element.getMeasurementTime().startsWith(DATE)) {
        receiver.output(element);
      }
    }
  }

  /**
   * Executes composite transformation on sensor readings:
   * 1) Filter all sensor readings that have property measurementTime equal to
   * DATE defined in interface "Constants"
   * 2) Filter filtered sensor readings that have dataStreamID property equal to
   * DATA_STREAM_ID defined in interface "Constants"
   * 3) Extract measurement title and units for filtered records
   * 4) Calculate maximal value for each record
   *
   * Returns maximal value for each measurement title and units.
   */
  public static class TaskCompositeTransformation
      extends PTransform<PCollection<SensorReading>, PCollection<OperationResult>> {
    @Override
    public PCollection<OperationResult> expand(PCollection<SensorReading> inputSensorReadings) {
      PCollection<SensorReading> filteredDateSensorReadings = inputSensorReadings
          .apply("Filter sensor readings with measurementTime property", ParDo.of(new FilterSensorReadingDateDoFn()));
      PCollection<SensorReading> filteredDataStreamIDSensorReadings = filteredDateSensorReadings
          .apply("Filter sensor readings with dataStreamID property",
              ParDo.of(new FilterSensorReadingDataStreamDoFn(Constants.DATA_STREAM_ID)));
      PCollection<KV<String, Double>> maximalMeasurementTitlesAndValues = filteredDataStreamIDSensorReadings
          .apply("PCollection for maximal value", ParDo.of(new ExtractMeasurementTitleAndUnitsDoFn("Maximal")));
      PCollection<KV<String, Double>> maximalValue = maximalMeasurementTitlesAndValues
          .apply("Calculate maximal value per key", Max.perKey());

      PCollection<KV<String, Double>> extractProcessingStartTime = inputSensorReadings
          .apply("Extract minimal processing start time",
              new ExtractMinimalStartTime(Constants.PROCESSING_START_TIME_NAME));
      PCollection<KV<String, Double>> extractKafkaInputTime = inputSensorReadings
          .apply("Extract minimal kafka input time", new ExtractMinimalStartTime(Constants.KAFKA_START_TIME_NAME));

      PCollection<KV<String, Double>> flattenCollection = PCollectionList
          .of(extractProcessingStartTime)
          .and(extractKafkaInputTime)
          .and(maximalValue)
          .apply("Flatten multiple collections to single PCollection", Flatten.<KV<String, Double>>pCollections());

      return flattenCollection.apply("Generate operation result", Combine.globally((new OperationResultFn())));
    }
  }
  // ******************************************* 2e) IDENTITY
  // *********************************************************

  /**
   * Returns number of sensor reading records.
   */
  public static class TaskIdentity extends PTransform<PCollection<SensorReading>, PCollection<OperationResult>> {
    @Override
    public PCollection<OperationResult> expand(PCollection<SensorReading> inputSensorReadings) {
      PCollection<Long> countSensorReadingRecords = inputSensorReadings
          .apply("Count number of sensor reading records globally", Count.globally());
      PCollection<KV<String, Double>> extractCountSensorReading = countSensorReadingRecords
          .apply("Extract count and transform to key-value PCollection",
              ParDo.of(new TransformLongToKVDoFn("Task Identity - Count")));

      PCollection<KV<String, Double>> extractProcessingStartTime = inputSensorReadings
          .apply("Extract minimal processing start time",
              new ExtractMinimalStartTime(Constants.PROCESSING_START_TIME_NAME));
      PCollection<KV<String, Double>> extractKafkaInputTime = inputSensorReadings
          .apply("Extract minimal kafka input time", new ExtractMinimalStartTime(Constants.KAFKA_START_TIME_NAME));

      PCollection<KV<String, Double>> flattenCollection = PCollectionList
          .of(extractProcessingStartTime)
          .and(extractKafkaInputTime)
          .and(extractCountSensorReading)
          .apply("Flatten multiple collections to single PCollection", Flatten.<KV<String, Double>>pCollections());

      return flattenCollection.apply("Generate operation result", Combine.globally((new OperationResultFn())));
    }
  }

  // ******************************************* 2f) FILTRATION
  // *******************************************************
  /**
   * Helper class for filtering sensor readings.
   */
  public static class FilterSensorReadingDataStreamDoFn extends DoFn<SensorReading, SensorReading> {
    long filterID; // Sensor reading property "dataStreamID"

    public FilterSensorReadingDataStreamDoFn(long filterID) {
      this.filterID = filterID;
    }

    @ProcessElement
    public void processElement(@Element SensorReading element, OutputReceiver<SensorReading> receiver) {
      if (element.getDataStreamID() == this.filterID) {
        receiver.output(element);
      }
    }
  }

  /**
   * Returns number of sensor reading records that have dataStreamID property
   * equal to DATA_STREAM_ID defined in interface "Constants"
   */
  public static class TaskFiltration extends PTransform<PCollection<SensorReading>, PCollection<OperationResult>> {
    @Override
    public PCollection<OperationResult> expand(PCollection<SensorReading> inputSensorReadings) {
      PCollection<SensorReading> filteredSensorReadings = inputSensorReadings
          .apply("Filter sensor readings with dataStreamID property",
              ParDo.of(new FilterSensorReadingDataStreamDoFn(Constants.DATA_STREAM_ID)));
      PCollection<Long> countSensorReadingRecords = filteredSensorReadings
          .apply("Count number of sensor reading records globally", Count.globally());
      PCollection<KV<String, Double>> extractCountSensorReading = countSensorReadingRecords
          .apply("Extract count and transform to key-value PCollection",
              ParDo.of(new TransformLongToKVDoFn("Task Filtration - Count")));

      PCollection<KV<String, Double>> extractProcessingStartTime = inputSensorReadings
          .apply("Extract minimal processing start time",
              new ExtractMinimalStartTime(Constants.PROCESSING_START_TIME_NAME));
      PCollection<KV<String, Double>> extractKafkaInputTime = inputSensorReadings
          .apply("Extract minimal kafka input time", new ExtractMinimalStartTime(Constants.KAFKA_START_TIME_NAME));

      PCollection<KV<String, Double>> flattenCollection = PCollectionList
          .of(extractProcessingStartTime)
          .and(extractKafkaInputTime)
          .and(extractCountSensorReading)
          .apply("Flatten multiple collections to single PCollection", Flatten.<KV<String, Double>>pCollections());

      return flattenCollection.apply("Generate operation result", Combine.globally((new OperationResultFn())));
    }
  }

  // ******************************************************************************************************************

  // ******************************************* 3) APPEND END TIME
  // ***************************************************
  /**
   * Helper class for appending end time to operation result.
   */
  public static class AppendEndTimeDoFn extends DoFn<OperationResult, OperationResult> {
    @ProcessElement
    public void processElement(@Element OperationResult element, OutputReceiver<OperationResult> receiver) {
      OperationResult processingElement = OperationResult.copy(element); // Important: copy element!
      processingElement.setProcessingTimeEnd(System.currentTimeMillis());
      receiver.output(processingElement);
    }
  }

  /**
   * Append end time to operation result.
   */
  public static class AppendEndTimeToOperationResult
      extends PTransform<PCollection<OperationResult>, PCollection<OperationResult>> {
    @Override
    public PCollection<OperationResult> expand(PCollection<OperationResult> operationResult) {
      return operationResult.apply("Append end time to operation result", ParDo.of(new AppendEndTimeDoFn()));
    }
  }

  // ******************************************************************************************************************

  // ****************************************** 4) TRANSFORM TO OUTPUT DATA
  // *******************************************

  /**
   * Transform operation result to string.
   */
  public static class TransformOperationResultToString extends SimpleFunction<OperationResult, String> {
    @Override
    public String apply(OperationResult input) {
      return input.toString();
    }
  }
  // ******************************************************************************************************************

  /**
   * Main class that executes pipeline
   * 
   * @param args Beam pipeline options
   * @throws IOException can be thrown by cancel() method
   */
  public static void main(String[] args) throws IOException {
    System.out.println("Starting execution...");
    PipelineOptionsFactory.register(BeamOptions.class);
    BeamOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(BeamOptions.class);
    System.out.println("got my options: " + options.toString());
    Pipeline pipeline = Pipeline.create(options);
    CoderRegistry cr = pipeline.getCoderRegistry();
    System.out.println("got my pipe ready");
    // Register own defined coders
    cr.registerCoderForClass(SensorReading.class, SensorReading.CODER);
    cr.registerCoderForClass(OperationResult.class, OperationResult.CODER);
    cr.registerCoderForClass(Accum.class, Accum.CODER);

    List<TopicPartition> topicPartitions = new ArrayList();
    for(int i =0;i< NUM_OF_KAFKA_PARTITIONS;i++) {
	topicPartitions.add(new TopicPartition(INPUT_TOPIC_NAME, i));
    }
    
    // 1) Read data from Kafka "input" topic
	// You can also read like thi .withTopic(INPUT_TOPIC_NAME)
	// Read over partitions .withTopicPartitions(topicPartitions)
    PCollection<KafkaRecord<Long, String>> inputData = pipeline
        .apply("Read input data from Kafka topic", KafkaIO.<Long, String>read()
            .withBootstrapServers(BOOTSTRAP_SERVERS+","+BOOTSTRAP_SERVERS)
	    .withTopic(INPUT_TOPIC_NAME)
            .withKeyDeserializer(LongDeserializer.class)
            .withValueDeserializer(StringDeserializer.class)
            .withMaxNumRecords((long) (LINES * ITERATIONS * PERCENTAGE)));
    System.out.println("got my input data from kafka");

    // 2) Transform input data to sensor readings
    PCollection<SensorReading> sensorReadings = inputData
        .apply("Transform input data to senor reading", MapElements.via(new TransformInputDataToSensorReading()));
    System.out.println("converted to sensorReadings");

    // 3) Execute specific task to get operation result
    PCollection<OperationResult> operationResult;

    if (options.getTask().equals("aggregation")) { // Aggregation
      System.out.println("starting task aggregation");
      operationResult = sensorReadings
          .apply("Task: Aggregation", new TaskAggregation());
      System.out.println("finished task aggregation");
    } else if (options.getTask().equals("specificValues")) { // Specific values
      operationResult = sensorReadings
          .apply("Task: Specific values", new TaskSpecificValues());
    } else if (options.getTask().equals("filtration")) { // Filtration
      operationResult = sensorReadings
          .apply("Task: Filtration", new TaskFiltration());
    } else if (options.getTask().equals("identity")) { // Identity
      operationResult = sensorReadings
          .apply("Task: Identity", new TaskIdentity());
    } else if (options.getTask().equals("topN")) { // Top N
      operationResult = sensorReadings
          .apply("Task: Top N", new TaskTopN());
    } else if (options.getTask().equals("compositeTransformation")) {
      operationResult = sensorReadings
          .apply("Task: Composite transformation", new TaskCompositeTransformation()); // Composite transformation
    } else {
      System.out.println("Invalid task argument! Beam will compute task 'Identity' by default.");
      operationResult = sensorReadings
          .apply("Task: Identity", new TaskIdentity()); // Identity is default task for execution (if user didn't
                                                        // specify otherwise)
    }

    // 4) Append end time to operation result
    PCollection<OperationResult> appendedEndTimeOperationResult = operationResult
        .apply("Append end time to operation result", new AppendEndTimeToOperationResult());

    // 5) Transform operation result to string
    PCollection<String> outputData = appendedEndTimeOperationResult
        .apply("Transform operation result to string", MapElements.via(new TransformOperationResultToString()));

    // 6) Send result to Kafka "output" topic
    System.out.println("before writing to kafka");
    outputData
        .apply("Write output data to Kafka topic", KafkaIO.<Void, String>write()
            .withBootstrapServers(BOOTSTRAP_SERVERS)
            .withTopic(OUTPUT_TOPIC_NAME)
            .withKeySerializer(null)
            .withValueSerializer(StringSerializer.class)
            .values());

    System.out.println("after writing to kafka");
    PipelineResult result = pipeline.run(); // Run pipeline
    System.out.println("after pipeline has been run");

    try {
      System.out.println("Waiting until finish...");
      result.waitUntilFinish();
    } catch (Exception exc) {
      result.cancel();
      System.err.println("Error while executing Beam pipeline!" + exc.getMessage());
    }
  }
}
