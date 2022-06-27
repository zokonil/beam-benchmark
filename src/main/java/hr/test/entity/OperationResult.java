package hr.test.entity;

import hr.test.constants.Constants;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.CustomCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.coders.VarLongCoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Objects;

/**
 * Class for storing operation result
 */
public class OperationResult implements Serializable, Constants {
    private static final Coder<String> STRING_CODER = StringUtf8Coder.of();
    private static final Coder<Long> LONG_CODER = VarLongCoder.of();

    public String result;
    public long numberOfRecords;
    public long processingTimeStart;
    public long processingTimeEnd;
    public long kafkaInputTime;

    public static final Coder<OperationResult> CODER = new CustomCoder<OperationResult>() {
        @Override
        public void encode(OperationResult value, OutputStream outStream)
                throws IOException {
            STRING_CODER.encode(value.result, outStream);
            LONG_CODER.encode(value.numberOfRecords, outStream);
            LONG_CODER.encode(value.processingTimeStart, outStream);
            LONG_CODER.encode(value.processingTimeEnd, outStream);
            LONG_CODER.encode(value.kafkaInputTime, outStream);
        }

        @Override
        public OperationResult decode(InputStream inStream) throws IOException {
            String result = STRING_CODER.decode(inStream);
            Long numberOfRecords = LONG_CODER.decode(inStream);
            Long processingTimeStart = LONG_CODER.decode(inStream);
            Long processingTimeEnd = LONG_CODER.decode(inStream);
            Long kafkaInputTime = LONG_CODER.decode(inStream);

            return new OperationResult(
                    result,
                    numberOfRecords,
                    processingTimeStart,
                    processingTimeEnd,
                    kafkaInputTime);
        }

        @Override
        public Object structuralValue(OperationResult v) {
            return v;
        }
    };

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public long getNumberOfRecords() {
        return numberOfRecords;
    }

    public void setNumberOfRecords(long numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
    }

    public long getProcessingTimeStart() {
        return processingTimeStart;
    }

    public void setProcessingTimeStart(long processingTimeStart) {
        this.processingTimeStart = processingTimeStart;
    }

    public long getProcessingTimeEnd() {
        return processingTimeEnd;
    }

    public void setProcessingTimeEnd(long processingTimeEnd) {
        this.processingTimeEnd = processingTimeEnd;
    }

    public long getKafkaInputTime() {
        return kafkaInputTime;
    }

    public void setKafkaInputTime(long kafkaInputTime) {
        this.kafkaInputTime = kafkaInputTime;
    }

    public OperationResult(String result, long numberOfRecords, long processingTimeStart, long processingTimeEnd,
            long kafkaInputTime) {
        this.result = result;
        this.numberOfRecords = numberOfRecords;
        this.processingTimeStart = processingTimeStart;
        this.processingTimeEnd = processingTimeEnd;
        this.kafkaInputTime = kafkaInputTime;
    }

    public OperationResult() {
        this.result = null;
        this.numberOfRecords = 0L;
        this.processingTimeStart = -1L;
        this.processingTimeEnd = -1L;
        this.kafkaInputTime = -1L;
    }

    @Override
    public String toString() {
        return "OperationResult {" +
                "result='" + result + '\'' +
                ", " + NUMBER_OF_RECORDS + "=" + numberOfRecords +
                ", " + PROCESSING_START_TIME_NAME + "=" + processingTimeStart +
                ", " + PROCESSING_END_TIME_NAME + "=" + processingTimeEnd +
                ", " + KAFKA_START_TIME_NAME + "=" + kafkaInputTime +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, numberOfRecords, processingTimeStart, processingTimeEnd, kafkaInputTime);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        OperationResult operationResult = (OperationResult) obj;
        return Objects.equals(result, operationResult.result)
                && Objects.equals(numberOfRecords, operationResult.numberOfRecords)
                && Objects.equals(processingTimeStart, operationResult.processingTimeStart)
                && Objects.equals(processingTimeEnd, operationResult.processingTimeEnd)
                && Objects.equals(kafkaInputTime, operationResult.kafkaInputTime);
    }

    public static OperationResult copy(OperationResult operationResult) {
        OperationResult newOperationResult = new OperationResult();

        newOperationResult.result = operationResult.getResult();
        newOperationResult.numberOfRecords = operationResult.getNumberOfRecords();
        newOperationResult.processingTimeStart = operationResult.getProcessingTimeStart();
        newOperationResult.processingTimeEnd = operationResult.getProcessingTimeEnd();
        newOperationResult.kafkaInputTime = operationResult.getKafkaInputTime();

        return newOperationResult;
    }
}
