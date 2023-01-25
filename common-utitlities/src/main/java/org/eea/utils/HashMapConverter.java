package org.eea.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.AttributeConverter;
import java.util.Map;

public class HashMapConverter implements AttributeConverter<Map<String, Object>, String> {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(HashMapConverter.class);

    @Override
    public String convertToDatabaseColumn(Map<String, Object> hashmap) {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = null;
        try {
            jsonStr = objectMapper.writeValueAsString(hashmap);
        } catch (Exception e) {
            LOG.error("Error when converting hashmap to Json string. Message: {}", e.getMessage());
        }

        return jsonStr;
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String jsonStr) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> hashmap = null;
        try {
            hashmap = objectMapper.readValue(jsonStr, Map.class);
        } catch (Exception e) {
            LOG.error("Error when converting Json string to hashmap. Message: {}", e.getMessage());
        }
        return hashmap;
    }

}