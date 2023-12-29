package com.phemex.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonStringConverter {
    static final Logger LOG = LoggerFactory.getLogger(JsonStringConverter.class);
    static final JsonStringConverter INSTANCE = new JsonStringConverter();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final byte[] zeroByte = new byte[0];

    public JsonStringConverter() {
        this.objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        this.objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
    }

    public <T> String to(T input) {
        try {
            return this.objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException var3) {
            LOG.warn("Failed to convert [{}] to json", input, var3);
            return "ERR";
        }
    }

    public <T> byte[] toBytes(T input) {
        try {
            return this.objectMapper.writeValueAsBytes(input);
        } catch (JsonProcessingException var3) {
            LOG.warn("Failed to write [{}] to bytes", input, var3);
            return this.zeroByte;
        }
    }

    public Object from(byte[] b, TypeReference type) throws IOException {
        return this.objectMapper.readValue(b, type);
    }

    public Object from(String b, TypeReference type) throws IOException {
        return this.objectMapper.readValue(b, type);
    }

    public <T> T from(String src, Class<T> clazz) {
        try {
            return this.objectMapper.readValue(src, clazz);
        } catch (IOException var4) {
            LOG.warn("Failed to parse str [{}] to obj of type [{}]", new Object[]{src, clazz.getSimpleName(), var4});
            return null;
        }
    }

    public <T> T from(InputStream ins, Class<T> clazz) {
        try {
            return this.objectMapper.readValue(ins, clazz);
        } catch (IOException var4) {
            LOG.warn("Failed to parse bytes to obj of type [{}]", clazz.getSimpleName(), var4);
            return null;
        }
    }

    public <T> T from(byte[] bytes, Class<T> clazz) {
        try {
            return this.objectMapper.readValue(bytes, clazz);
        } catch (IOException var4) {
            LOG.warn("Failed to parse bytes [{}] to obj of type [{}]", new Object[]{bytes.length, clazz.getSimpleName(), var4});
            return null;
        }
    }

    public <T> T fromEx(byte[] bytes, Class<T> clazz) throws IOException {
        return this.objectMapper.readValue(bytes, clazz);
    }

    public <T> T fromEx(String str, Class<T> clazz) throws IOException {
        return this.objectMapper.readValue(str, clazz);
    }

    public String map2Json(Map<String, Object> obj) {
        try {
            return this.objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException var3) {
            LOG.warn("Failed to write map obj [{}] as string", obj, var3);
            return null;
        }
    }

    public <T> T map2Obj(Map<String, Object> objMap, Class<T> clazz) {
        return this.objectMapper.convertValue(objMap, clazz);
    }

    public <T> List<T> fromList(String src, Class<T> clazz) throws IOException {
        return (List)this.objectMapper.readValue(src, this.objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }

    public static JsonStringConverter instance() {
        return INSTANCE;
    }
}
