package hr.test.entity;

import hr.test.constants.Constants;
import org.apache.beam.sdk.coders.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Objects;

public class Accum implements Serializable, Constants {
    private static final Coder<String> STRING_CODER = StringUtf8Coder.of();
    private static final Coder<Double> DOUBLE_CODER = DoubleCoder.of();

    public String result;
    public double numberOfRecords;
    public double processingTimeStart;
    public double kafkaInputTime;

    public static final Coder<Accum> CODER = new CustomCoder<Accum>() {
        @Override
        public void encode(Accum value, OutputStream outStream)
                throws IOException {
            STRING_CODER.encode(value.result, outStream);
            DOUBLE_CODER.encode(value.numberOfRecords, outStream);
            DOUBLE_CODER.encode(value.processingTimeStart, outStream);
            DOUBLE_CODER.encode(value.kafkaInputTime, outStream);
        }

        @Override
        public Accum decode(InputStream inStream) throws IOException {
            String result = STRING_CODER.decode(inStream);
            Double numberOfRecords = DOUBLE_CODER.decode(inStream);
            Double processingTimeStart = DOUBLE_CODER.decode(inStream);
            Double kafkaInputTime = DOUBLE_CODER.decode(inStream);

            return new Accum(
                    result,
                    numberOfRecords,
                    processingTimeStart,
                    kafkaInputTime);
        }

        @Override
        public Object structuralValue(Accum v) {
            return v;
        }
    };

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public double getNumberOfRecords() {
        return numberOfRecords;
    }

    public void setNumberOfRecords(double numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
    }

    public double getProcessingTimeStart() {
        return processingTimeStart;
    }

    public void setProcessingTimeStart(double processingTimeStart) {
        this.processingTimeStart = processingTimeStart;
    }

    public double getKafkaInputTime() {
        return kafkaInputTime;
    }

    public void setKafkaInputTime(double kafkaInputTime) {
        this.kafkaInputTime = kafkaInputTime;
    }

    public Accum(String result, double numberOfRecords, double processingTimeStart, double kafkaInputTime) {
        this.result = result;
        this.numberOfRecords = numberOfRecords;
        this.processingTimeStart = processingTimeStart;
        this.kafkaInputTime = kafkaInputTime;
    }

    public Accum() {
        this.result = "";
        this.numberOfRecords = 0;
        this.processingTimeStart = 0;
        this.kafkaInputTime = 0;
    }

    @Override
    public String toString() {
        return "Accum {" +
                "result='" + result + '\'' +
                ", " + NUMBER_OF_RECORDS + "=" + numberOfRecords +
                ", " + PROCESSING_START_TIME_NAME + "=" + processingTimeStart +
                ", " + KAFKA_START_TIME_NAME + "=" + kafkaInputTime +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, numberOfRecords, processingTimeStart, kafkaInputTime);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Accum accum = (Accum) obj;
        return Objects.equals(result, accum.result)
                && Objects.equals(numberOfRecords, accum.numberOfRecords)
                && Objects.equals(processingTimeStart, accum.processingTimeStart)
                && Objects.equals(kafkaInputTime, accum.kafkaInputTime);
    }

}
