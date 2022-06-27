package hr.test.entity;

import hr.test.constants.Constants;
import org.apache.beam.sdk.coders.*;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Objects;

/**
 * Class for storing input data
 */
public class SensorReading implements Serializable, Constants {
    private static final Coder<String> STRING_CODER = StringUtf8Coder.of();
    private static final Coder<Long> LONG_CODER = VarLongCoder.of();
    private static final Coder<Boolean> BOOLEAN_CODER = BooleanCoder.of();

    public long ID; // Entry ID

    /* Measurement data */
    public String measurementTitle;
    public String measurementDescription;
    public String measurementType;
    public String measurementMedium;
    public String measurementTime;
    public String measurementValue;
    public String units;
    public String unitsAbbreviation;
    public String measurementPeriodType;
    public long dataStreamID;
    public long resourceID;
    public String measurementID;
    public String recordID;
    public String latitude;
    public String longitude;
    public String location;

    /* Time data */
    public long processingTimeStart;
    public long processingTimeEnd;
    public long kafkaInputTime;

    /* Filtration */
    public boolean filtered;

    public static final Coder<SensorReading> CODER = new CustomCoder<SensorReading>() {
        @Override
        public void encode(SensorReading value, OutputStream outStream)
                throws IOException {
            LONG_CODER.encode(value.ID, outStream);
            STRING_CODER.encode(value.measurementTitle, outStream);
            STRING_CODER.encode(value.measurementDescription, outStream);
            STRING_CODER.encode(value.measurementType, outStream);
            STRING_CODER.encode(value.measurementMedium, outStream);
            STRING_CODER.encode(value.measurementTime, outStream);
            STRING_CODER.encode(value.measurementValue, outStream);
            STRING_CODER.encode(value.units, outStream);
            STRING_CODER.encode(value.unitsAbbreviation, outStream);
            STRING_CODER.encode(value.measurementPeriodType, outStream);
            LONG_CODER.encode(value.dataStreamID, outStream);
            LONG_CODER.encode(value.resourceID, outStream);
            STRING_CODER.encode(value.measurementID, outStream);
            STRING_CODER.encode(value.recordID, outStream);
            STRING_CODER.encode(value.latitude, outStream);
            STRING_CODER.encode(value.longitude, outStream);
            STRING_CODER.encode(value.location, outStream);
            LONG_CODER.encode(value.processingTimeStart, outStream);
            LONG_CODER.encode(value.processingTimeEnd, outStream);
            LONG_CODER.encode(value.kafkaInputTime, outStream);
            BOOLEAN_CODER.encode(value.filtered, outStream);
        }

        @Override
        public SensorReading decode(InputStream inStream) throws IOException {
            long ID = LONG_CODER.decode(inStream);
            String measurementTitle = STRING_CODER.decode(inStream);
            String measurementDescription = STRING_CODER.decode(inStream);
            String measurementType = STRING_CODER.decode(inStream);
            String measurementMedium = STRING_CODER.decode(inStream);
            String measurementTime = STRING_CODER.decode(inStream);
            String measurementValue = STRING_CODER.decode(inStream);
            String units = STRING_CODER.decode(inStream);
            String unitsAbbreviation = STRING_CODER.decode(inStream);
            String measurementPeriodType = STRING_CODER.decode(inStream);
            Long dataStreamID = LONG_CODER.decode(inStream);
            Long resourceID = LONG_CODER.decode(inStream);
            String measurementID = STRING_CODER.decode(inStream);
            String recordID = STRING_CODER.decode(inStream);
            String latitude = STRING_CODER.decode(inStream);
            String location = STRING_CODER.decode(inStream);
            String longitude = STRING_CODER.decode(inStream);
            Long processingTimeStart = LONG_CODER.decode(inStream);
            Long processingTimeEnd = LONG_CODER.decode(inStream);
            Long kafkaInputTime = LONG_CODER.decode(inStream);
            boolean filtered = BOOLEAN_CODER.decode(inStream);

            return new SensorReading(
                    ID,
                    measurementTitle,
                    measurementDescription,
                    measurementType,
                    measurementMedium,
                    measurementTime,
                    measurementValue,
                    units,
                    unitsAbbreviation,
                    measurementPeriodType,
                    dataStreamID,
                    resourceID,
                    measurementID,
                    recordID,
                    latitude,
                    longitude,
                    location,
                    processingTimeStart,
                    processingTimeEnd,
                    kafkaInputTime,
                    filtered);
        }

        @Override
        public Object structuralValue(SensorReading v) {
            return v;
        }
    };

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public String getMeasurementTitle() {
        return measurementTitle;
    }

    public void setMeasurementTitle(String measurementTitle) {
        this.measurementTitle = measurementTitle;
    }

    public String getMeasurementDescription() {
        return measurementDescription;
    }

    public void setMeasurementDescription(String measurementDescription) {
        this.measurementDescription = measurementDescription;
    }

    public String getMeasurementType() {
        return measurementType;
    }

    public void setMeasurementType(String measurementType) {
        this.measurementType = measurementType;
    }

    public String getMeasurementMedium() {
        return measurementMedium;
    }

    public void setMeasurementMedium(String measurementMedium) {
        this.measurementMedium = measurementMedium;
    }

    public String getMeasurementTime() {
        return measurementTime;
    }

    public void setMeasurementTime(String measurementTime) {
        this.measurementTime = measurementTime;
    }

    public String getMeasurementValue() {
        return measurementValue.replaceAll("\"", "");
    }

    public void setMeasurementValue(String measurementValue) {
        this.measurementValue = measurementValue;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getUnitsAbbreviation() {
        return unitsAbbreviation;
    }

    public void setUnitsAbbreviation(String unitsAbbreviation) {
        this.unitsAbbreviation = unitsAbbreviation;
    }

    public String getMeasurementPeriodType() {
        return measurementPeriodType;
    }

    public void setMeasurementPeriodType(String measurementPeriodType) {
        this.measurementPeriodType = measurementPeriodType;
    }

    public long getDataStreamID() {
        return dataStreamID;
    }

    public void setDataStreamID(long dataStreamID) {
        this.dataStreamID = dataStreamID;
    }

    public long getResourceID() {
        return resourceID;
    }

    public void setResourceID(long resourceID) {
        this.resourceID = resourceID;
    }

    public String getMeasurementID() {
        return measurementID;
    }

    public void setMeasurementID(String measurementID) {
        this.measurementID = measurementID;
    }

    public String getRecordID() {
        return recordID;
    }

    public void setRecordID(String recordID) {
        this.recordID = recordID;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public boolean isFiltered() {
        return filtered;
    }

    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }

    public SensorReading(long ID, String measurementTitle, String measurementDescription, String measurementType,
            String measurementMedium, String measurementTime, String measurementValue, String units,
            String unitsAbbreviation, String measurementPeriodType, Long dataStreamID, Long resourceID,
            String measurementID, String recordID, String latitude, String longitude, String location,
            long processingTimeStart, long processingTimeEnd, long kafkaInputTime, boolean filtered) {
        this.ID = ID;
        this.measurementTitle = measurementTitle;
        this.measurementDescription = measurementDescription;
        this.measurementType = measurementType;
        this.measurementMedium = measurementMedium;
        this.measurementTime = measurementTime;
        this.measurementValue = measurementValue;
        this.units = units;
        this.unitsAbbreviation = unitsAbbreviation;
        this.measurementPeriodType = measurementPeriodType;
        this.dataStreamID = dataStreamID;
        this.resourceID = resourceID;
        this.measurementID = measurementID;
        this.recordID = recordID;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
        this.processingTimeStart = processingTimeStart;
        this.processingTimeEnd = processingTimeEnd;
        this.kafkaInputTime = kafkaInputTime;
        this.filtered = filtered;
    }

    public SensorReading() {
        this.ID = 0;
        this.measurementTitle = null;
        this.measurementDescription = null;
        this.measurementType = null;
        this.measurementMedium = null;
        this.measurementTime = null;
        this.measurementValue = null;
        this.units = null;
        this.unitsAbbreviation = null;
        this.measurementPeriodType = null;
        this.dataStreamID = -1L;
        this.resourceID = -1L;
        this.measurementID = null;
        this.recordID = null;
        this.latitude = null;
        this.longitude = null;
        this.location = null;
        this.processingTimeStart = -1L;
        this.processingTimeEnd = -1L;
        this.kafkaInputTime = -1L;
        this.filtered = true;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SensorReading sensorReading = (SensorReading) obj;
        return ID == sensorReading.ID
                && Objects.equals(measurementTitle, sensorReading.measurementTitle)
                && Objects.equals(measurementDescription, sensorReading.measurementDescription)
                && Objects.equals(measurementType, sensorReading.measurementType)
                && Objects.equals(measurementMedium, sensorReading.measurementMedium)
                && Objects.equals(measurementTime, sensorReading.measurementTime)
                && Objects.equals(measurementValue, sensorReading.measurementValue)
                && Objects.equals(units, sensorReading.units)
                && Objects.equals(unitsAbbreviation, sensorReading.unitsAbbreviation)
                && Objects.equals(measurementPeriodType, sensorReading.measurementPeriodType)
                && Objects.equals(dataStreamID, sensorReading.dataStreamID)
                && Objects.equals(resourceID, sensorReading.resourceID)
                && Objects.equals(measurementID, sensorReading.measurementID)
                && Objects.equals(recordID, sensorReading.recordID)
                && Objects.equals(latitude, sensorReading.latitude)
                && Objects.equals(longitude, sensorReading.longitude)
                && Objects.equals(location, sensorReading.location)
                && Objects.equals(filtered, sensorReading.filtered);
    }

    @Override
    public String toString() {
        return "SensorReading {" +
                "ID=" + ID +
                ", measurementTitle='" + measurementTitle + '\'' +
                ", measurementDescription='" + measurementDescription + '\'' +
                ", measurementType='" + measurementType + '\'' +
                ", measurementMedium='" + measurementMedium + '\'' +
                ", measurementTime='" + measurementTime + '\'' +
                ", measurementValue='" + measurementValue + '\'' +
                ", units='" + units + '\'' +
                ", unitsAbbreviation='" + unitsAbbreviation + '\'' +
                ", measurementPeriodType='" + measurementPeriodType + '\'' +
                ", dataStreamID=" + dataStreamID +
                ", resourceID=" + resourceID +
                ", measurementID='" + measurementID + '\'' +
                ", recordID='" + recordID + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", location='" + location + '\'' +
                ", " + PROCESSING_START_TIME_NAME + "=" + processingTimeStart +
                ", " + PROCESSING_END_TIME_NAME + "=" + processingTimeEnd +
                ", " + KAFKA_START_TIME_NAME + "=" + kafkaInputTime +
                ", filtered=" + filtered +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, measurementTitle, measurementDescription, measurementType, measurementMedium,
                measurementTime, measurementValue, units, unitsAbbreviation, measurementPeriodType, dataStreamID,
                resourceID, measurementID, recordID, latitude, longitude, location, filtered);
    }

    public static SensorReading copy(SensorReading sensorReading) {
        SensorReading newSensorReading = new SensorReading();

        newSensorReading.ID = sensorReading.ID;
        newSensorReading.measurementTitle = sensorReading.getMeasurementTitle();
        newSensorReading.measurementDescription = sensorReading.getMeasurementDescription();
        newSensorReading.measurementType = sensorReading.getMeasurementType();
        newSensorReading.measurementMedium = sensorReading.getMeasurementMedium();
        newSensorReading.measurementTime = sensorReading.getMeasurementTime();
        newSensorReading.measurementValue = sensorReading.getMeasurementValue();
        newSensorReading.units = sensorReading.getUnits();
        newSensorReading.unitsAbbreviation = sensorReading.getUnitsAbbreviation();
        newSensorReading.measurementPeriodType = sensorReading.getMeasurementPeriodType();
        newSensorReading.dataStreamID = sensorReading.getDataStreamID();
        newSensorReading.resourceID = sensorReading.getResourceID();
        newSensorReading.measurementID = sensorReading.getMeasurementID();
        newSensorReading.recordID = sensorReading.getRecordID();
        newSensorReading.latitude = sensorReading.getLatitude();
        newSensorReading.longitude = sensorReading.getLongitude();
        newSensorReading.location = sensorReading.getLocation();
        newSensorReading.processingTimeStart = sensorReading.getProcessingTimeStart();
        newSensorReading.processingTimeEnd = sensorReading.getProcessingTimeEnd();
        newSensorReading.kafkaInputTime = sensorReading.getKafkaInputTime();
        newSensorReading.filtered = sensorReading.isFiltered();

        return newSensorReading;
    }
}
