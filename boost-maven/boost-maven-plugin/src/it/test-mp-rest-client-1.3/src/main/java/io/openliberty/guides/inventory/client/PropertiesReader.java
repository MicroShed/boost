package io.openliberty.guides.inventory.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

public class PropertiesReader implements MessageBodyReader<Properties>, MessageBodyWriter<Properties> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public Properties readFrom(Class<Properties> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {

        JsonReader jr = Json.createReader(entityStream);
        JsonObject json = jr.readObject();
        Properties retVal = new Properties();

        json.keySet().forEach(key -> {
            JsonValue value = json.get(key);
            if (!JsonValue.NULL.equals(value)) {
                if (value.getValueType() != JsonValue.ValueType.STRING) {
                    throw new IllegalArgumentException(
                            "Non-String JSON prop value found in payload.  Sample data is more than this sample can deal with.  It's not intended to handle any payload.");
                }
                JsonString jstr = (JsonString) value;

                retVal.setProperty(key, jstr.getString());
            }
        });
        return retVal;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void writeTo(Properties t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        // TODO Auto-generated method stub

    }

}
