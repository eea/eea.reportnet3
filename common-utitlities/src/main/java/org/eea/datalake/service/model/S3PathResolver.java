package org.eea.datalake.service.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


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

    public S3PathResolver(long dataflowId, long datasetId,
        String tableName, String path) {
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
}
