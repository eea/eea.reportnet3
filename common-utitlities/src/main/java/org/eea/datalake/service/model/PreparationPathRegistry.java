package org.eea.datalake.service.model;

import org.eea.utils.LiteralConstants;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility registry responsible for resolving S3 path templates when the
 * preparation feature is used.
 *
 * <p>
 * The registry inspects {@link LiteralConstants} at class initialization time
 * and automatically maps standard S3 path templates to their corresponding
 * preparation templates based on naming conventions.
 * </p>
 *
 * <p>
 * Mapping is derived from the following convention:
 * </p>
 *
 * <pre>
 * S3_PREPARATION_*  → preparation path
 * S3_*              → corresponding base path
 * </pre>
 *
 * <p>
 * If a preparation path exists for a given base path, it replaces the
 * original template during resolution.
 * </p>
 *
 * <p>
 * Initialization is performed once at class loading time and lookups are
 * constant-time via an internal {@link Map}.
 * </p>
 */
public final class PreparationPathRegistry {

    private static final Map<String, String> PREPARATION_PATHS = new HashMap<>();

    private static final String S3_PREFIX = "S3_";
    private static final String PREPARATION_PREFIX = "S3_PREPARATION_";

    static {

        try {
            Field[] fields = LiteralConstants.class.getDeclaredFields();

            // First resolve only preparation constants
            for (Field field : fields) {

                if (field.getType() != String.class)
                    continue;

                String fieldName = field.getName();

                if (!fieldName.startsWith(PREPARATION_PREFIX))
                    continue;

                String parentName =
                        S3_PREFIX + fieldName.substring(PREPARATION_PREFIX.length());

                try {
                    Field parentField =
                            LiteralConstants.class.getDeclaredField(parentName);

                    String parentPath = (String) parentField.get(null);
                    String preparationPath = (String) field.get(null);

                    // Last write wins — duplicates automatically overwrite
                    PREPARATION_PATHS.put(parentPath, preparationPath);
                    System.out.println("[CHRIS]" + parentField.getName() + " -> " + field.getName());

                } catch (NoSuchFieldException | IllegalAccessException ignored) {
                    // If parent doesn't exist, skip silently. Registry remains consistent with available constants.
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed initializing preparation path registry", e);
        }
    }

    private PreparationPathRegistry() {
    }

    /**
     * Resolves the S3 path template.
     *
     * <pre>
     * Example:
     * from
     *     S3_PROVIDER_PATH             → given path
     * it returns
     *     S3_PREPARATION_PROVIDER_PATH → corresponding preparation path
     * </pre>
     *
     * @param path base path template
     * @param preparationCode preparation identifier
     * @return preparation path if available and preparation is enabled,
     *         otherwise the original path

     */
    public static String resolve(String path, String preparationCode) {

        if (preparationCode == null || preparationCode.isBlank()) {
            return path;
        }

        return PREPARATION_PATHS.getOrDefault(path, path);
    }
}