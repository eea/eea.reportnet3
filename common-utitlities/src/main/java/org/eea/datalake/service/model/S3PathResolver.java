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


    public S3PathResolver(long dataflowId, long dataProviderId, long datasetId) {
        this.dataflowId = dataflowId;
        this.dataProviderId = dataProviderId;
        this.datasetId = datasetId;
    }

    public S3PathResolver(long dataflowId, long dataProviderId, long datasetId,
        String tableName, String filename) {
        this(dataflowId, dataProviderId, datasetId);
        this.tableName = tableName;
        this.filename = filename;
    }

    public S3PathResolver(long dataflowId, long dataProviderId, long datasetId,
        String tableName, String filename, long validationId) {
        this(dataflowId, dataProviderId, datasetId);
        this.tableName = tableName;
        this.filename = filename;
        this.validationId = validationId;
    }

    public S3PathResolver(long dataflowId) {
        this.dataflowId = dataflowId;
    }

    public S3PathResolver(long dataflowId, long dataProviderId, long datasetId,
                          String tableName) {
        this(dataflowId, dataProviderId, datasetId);
        this.tableName = tableName;
    }
}
