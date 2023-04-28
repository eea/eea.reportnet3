package org.eea.dataset.persistence.data.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonValidator {
    public static boolean isValidJson(String json) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
