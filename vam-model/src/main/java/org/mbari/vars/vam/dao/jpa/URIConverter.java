package org.mbari.vars.vam.dao.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by brian on 5/12/16.
 */
@Converter(autoApply = true)
public class URIConverter implements AttributeConverter<URI, String> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public String convertToDatabaseColumn(URI uri) {
        return uri == null ? null : uri.toString();
    }

    @Override
    public URI convertToEntityAttribute(String s) {
        URI uri = null;
        if (s != null) {
            try {
                uri = new URI(s);
            }
            catch (URISyntaxException e) {
                log.warn("Bad URI found. Could not convert " + s + " to a URI");
            }
        }

        return uri;
    }
}
