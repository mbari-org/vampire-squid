package org.mbari.vars.vam.json;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Base64;

/**
 * @author Brian Schlining
 * @since 2017-03-02T09:54:00
 */
public class ByteArrayConverter
        implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {

    @Override
    public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Base64.getDecoder().decode(json.getAsString());
    }

    @Override
    public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(Base64.getEncoder().encodeToString(src));
    }
}
