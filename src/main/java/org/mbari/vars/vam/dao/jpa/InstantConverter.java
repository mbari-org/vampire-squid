package org.mbari.vars.vam.dao.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * Created by brian on 5/12/16.
 * @see https://stackoverflow.com/questions/22415173/how-to-store-date-time-in-utc-into-a-database-using-eclipselink-and-joda-time
 */
@Converter(autoApply = true)
public class InstantConverter implements AttributeConverter<Instant, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(Instant instant) {
        return instant == null ? null : new Timestamp(instant.toEpochMilli());
    }

    @Override
    public Instant convertToEntityAttribute(Timestamp timestamp) {
        return timestamp == null ? null : Instant.ofEpochMilli(timestamp.getTime());
    }
}
