/*
 * Copyright 2021 Monterey Bay Aquarium Research Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mbari.vampiresquid.etc.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
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
