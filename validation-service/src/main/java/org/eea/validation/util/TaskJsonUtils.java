package org.eea.validation.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TaskJsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static Long getDatasetId(String taskJson) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(taskJson);
            JsonNode datasetIdNode = root.path("data").path("dataset_id");

            if (!datasetIdNode.isMissingNode()) {
                return datasetIdNode.asLong();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract dataset_id from task json", e);
        }

        return null;
    }

    public static String getProcessId(String taskJson) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(taskJson);
            JsonNode processIdNode = root.path("data").path("processId");

            if (!processIdNode.isMissingNode()) {
                return processIdNode.asText();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract processId from task json", e);
        }

        return null;
    }

    public static boolean isBlockerTask(String taskJson) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(taskJson);
            String ruleLevelError =
                    root.path("data")
                        .path("ruleLevelError")
                        .asText();

            return "BLOCKER".equalsIgnoreCase(ruleLevelError);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract ruleLevelError from task json", e);
        }
    }
}