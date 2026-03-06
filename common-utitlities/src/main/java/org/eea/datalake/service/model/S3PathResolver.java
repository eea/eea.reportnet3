package org.eea.datalake.service.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * Model used for resolving S3 storage paths for datasets, providers,
 * tables, validation outputs, and other data lake resources.
 *
 * <p>
 * The resolver encapsulates all parameters required to construct
 * S3 path templates defined in {@link org.eea.utils.LiteralConstants}.
 * </p>
 *
 * <p>
 * It also supports the preparation dataset feature, allowing the
 * resolution of preparation-specific paths when a preparation code
 * is provided.
 * </p>
 */
@Getter
@Setter
@ToString
public class S3PathResolver {

    /** The dataflow id. */
    private long dataflowId;

    /** The data provider id. */
    private long dataProviderId;

    /** The dataset id. */
    private long datasetId;

    /** The table name of dataset. */
    private String tableName;

    /** The filename of parquet file. */
    private String filename;

    /** The validation id. */
    private long validationId;

    /** The path. */
    private String path;

    /** The parquet folder. */
    private String parquetFolder;

    /** The snapshot id. */
    private long snapshotId;

    /** The data provider name. */
    private String dataProviderName;

    /** The deleteFile. */
    private boolean deleteFile;

    /** The isIcebergTable. */
    private Boolean isIcebergTable;

    /** Name of the preparation dataset, if applicable */
    private String preparationCode;

    public S3PathResolver(long dataflowId, long dataProviderId, long datasetId,
                          String tableName, String filename, String preparationCode, String path) {
        this(dataflowId, dataProviderId, datasetId, tableName, filename, path);
        this.preparationCode = preparationCode;
        if (StringUtils.isNotBlank(preparationCode)) this.path = getResolvedPath();
    }

    public S3PathResolver(long dataflowId, long dataProviderId, long datasetId) {
        this.dataflowId = dataflowId;
        this.dataProviderId = dataProviderId;
        this.datasetId = datasetId;
    }

    public S3PathResolver(long dataflowId, long datasetId) {
        this.dataflowId = dataflowId;
        this.datasetId = datasetId;
    }

    public S3PathResolver(long dataflowId, long dataProviderId, long datasetId,
        String tableName, String filename) {
        this(dataflowId, dataProviderId, datasetId);
        this.tableName = tableName;
        this.filename = filename;
    }

    public S3PathResolver(long dataflowId, long dataProviderId, long datasetId,
        String tableName, String filename, String path) {
        this(dataflowId, dataProviderId, datasetId);
        this.tableName = tableName;
        this.filename = filename;
        this.path = path;
    }

    public S3PathResolver(long dataflowId) {
        this.dataflowId = dataflowId;
    }

    public S3PathResolver(long dataflowId, String tableName) {
        this.dataflowId = dataflowId;
        this.tableName = tableName;
    }

    public S3PathResolver(long dataflowId, long dataProviderId, long datasetId,
                          String tableName) {
        this(dataflowId, dataProviderId, datasetId);
        this.tableName = tableName;
    }

    public S3PathResolver(long dataflowId, long datasetId, String tableName, String path) {
        this(dataflowId, datasetId);
        this.tableName = tableName;
        this.path = path;
    }

    public S3PathResolver(Long dataflowId, Long datasetId, String path) {
        this.dataflowId = dataflowId;
        this.datasetId = datasetId;
        this.path = path;
    }

    public S3PathResolver(long dataflowId, String filename, String path) {
        this.dataflowId = dataflowId;
        this.filename = filename;
        this.path = path;
    }

    /**
     * Returns the resolved S3 path template taking into account the
     * preparation dataset feature.
     *
     * <p>
     * If a preparation code is defined, the method attempts to resolve the
     * corresponding preparation path template using
     * {@link PreparationPathRegistry}. Otherwise, the original path
     * template is returned unchanged.
     * </p>
     *
     * <p>
     * This allows the same resolver instance to transparently support both
     * standard dataset paths and preparation dataset paths.
     * </p>
     *
     * @return the resolved S3 path template
     */
    public String getResolvedPath() {
        return PreparationPathRegistry.resolve(path, preparationCode);
    }
}
