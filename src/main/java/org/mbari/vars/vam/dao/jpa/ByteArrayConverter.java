package org.mbari.vars.vam.dao.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Base64;

/**
 * @author Brian Schlining
 * @since 2017-03-02T10:57:00
 */
@Converter(autoApply = true)
public class ByteArrayConverter implements AttributeConverter<byte[], String> {

    @Override
    public String convertToDatabaseColumn(byte[] bs) {
        return bs == null ? null : Base64.getEncoder().encodeToString(bs);
    }

    @Override
    public byte[] convertToEntityAttribute(String s) {
        return s == null ? null : Base64.getDecoder().decode(s);
    }
}

