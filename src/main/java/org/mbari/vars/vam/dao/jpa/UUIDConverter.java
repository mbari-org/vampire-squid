package org.mbari.vars.vam.dao.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.UUID;

/**
 * Created by brian on 5/12/16.
 */
@Converter(autoApply = true)
public class UUIDConverter implements AttributeConverter<UUID, String> {

    @Override
    public String convertToDatabaseColumn(UUID uuid) {
        return uuid == null ? null : uuid.toString().toUpperCase();
    }

    @Override
    public UUID convertToEntityAttribute(String s) {
        return s == null ? null : UUID.fromString(s.toUpperCase());
    }
}
