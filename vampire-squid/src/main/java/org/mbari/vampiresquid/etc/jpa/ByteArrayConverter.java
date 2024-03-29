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

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.HexFormat;


/**
 * @author Brian Schlining
 * @since 2017-03-02T10:57:00
 */
@Converter(autoApply = true)
public class ByteArrayConverter implements AttributeConverter<byte[], String> {

    private static final HexFormat hexFormat = HexFormat.of();

    @Override
    public String convertToDatabaseColumn(byte[] bs) {
        return bs == null ? null : encode(bs);
    }

    @Override
    public byte[] convertToEntityAttribute(String s) {
        return s == null ? null : decode(s);
    }

    public static String encode(byte[] bs) {
        return hexFormat.formatHex(bs);
//        return DatatypeConverter.printHexBinary(bs);
    }

    public static byte[] decode(String s) {
        return hexFormat.parseHex(s);
//        return DatatypeConverter.parseHexBinary(s);
    }
}

