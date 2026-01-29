package org.eea.dataset.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.bson.types.ObjectId;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.SpatialDataHandling;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.CreateEmptyTables;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.eea.utils.LiteralConstants.*;

@Service
@RequiredArgsConstructor
public class CreateEmptyTablesImpl implements CreateEmptyTables {

    private final S3Helper s3Helper;
    private final DremioHelperService dremioHelperService;
    private final SpatialDataHandling spatialDataHandling;
    private final SchemasRepository schemasRepository;

    @Autowired
    private JdbcTemplate dremioJdbcTemplate;

    private static final Logger LOG = LoggerFactory.getLogger(CreateEmptyTablesImpl.class);

    final String ILLEGAL_CHAR_MARKER = "Illegal character in";

    @Value("${parquet.file.path}")
    private String parquetFilePath;

    @Override
    public void runCreationForOneDataset(DataSetMetabaseVO dataset) throws EEAException {
        DataSetSchema schema = schemasRepository.findByIdDataSetSchema(new ObjectId(dataset.getDatasetSchema()));

        for (TableSchema tableSchema : schema.getTableSchemas()) {
            processTableSchema(dataset, tableSchema);
        }
    }

    @Override
    public void runCreationForSpecificTableSchema(DataSetMetabaseVO dataset, String tableSchemaId) throws EEAException {
        DataSetSchema schema = schemasRepository.findByIdDataSetSchema(new ObjectId(dataset.getDatasetSchema()));
        TableSchema targetTableSchema = schema.getTableSchemas().stream()
                .filter(table -> table.getIdTableSchema().toString().equals(tableSchemaId))
                .findFirst()
                .orElseThrow(() -> new EEAException("Table schema with ID " + tableSchemaId + " not found"));
        processTableSchema(dataset, targetTableSchema);
    }

    private void processTableSchema(DataSetMetabaseVO dataset, TableSchema tableSchema) throws EEAException {
        S3PathResolver s3TablePathResolver = new S3PathResolver(
                dataset.getDataflowId(),
                dataset.getDataProviderId() != null ? dataset.getDataProviderId() : 0L,
                dataset.getId(),
                tableSchema.getNameTableSchema(),
                tableSchema.getNameTableSchema(),
                getRightPath(dataset, true)
        );

        try {
            if (dataset.getDatasetTypeEnum().equals(DatasetTypeEnum.DESIGN)) {
                deleteTableIfEmpty(tableSchema.getNameTableSchema(), s3TablePathResolver);
            }
            boolean folderExists = s3Helper.checkFolderExist(s3TablePathResolver, getRightPath(dataset, false));
            if (!folderExists) {
                createEmptyTableWithFields(dataset, tableSchema, s3TablePathResolver);
            }
        } catch (EEAException eea) {
            throw eea;
        } catch (Exception e) {
            LOG.error("Something went wrong, trying to create empty tables for dataflowId {} and datasetId {} , with exception message: {}", dataset.getDataflowId(), dataset.getId(), e.getMessage());
            throw new EEAException("Something went wrong, trying to create empty tables with message: " + e.getMessage());
        }
    }

    /**
     * Extracted method: regenerates parquet table and uploads to S3.
     * Used for both normal and preparation datasets.
     */
    protected void regenerateTable(DataSetMetabaseVO dataset, TableSchema tableSchema, List<Schema.Field> fields, S3PathResolver s3PathResolver) throws Exception {
        String file = "0_0_0.parquet";
        String parquetFile = parquetFilePath + file;
        try {
            Schema schema1 = Schema.createRecord("Data", null, null, false, fields);

            try {
                dremioHelperService.deleteFileFromR3IfExists(parquetFile);
                try (ParquetWriter<GenericRecord> writer = AvroParquetWriter
                        .<GenericRecord>builder(new Path(parquetFile))
                        .withSchema(schema1)
                        .withCompressionCodec(CompressionCodecName.SNAPPY)
                        .withPageSize(4 * 1024)
                        .withRowGroupSize(16 * 1024)
                        .build()) {
                } catch (Exception e1) {
                    LOG.error("Error creating parquet file {},{}", parquetFile, e1.getMessage());
                    throw new EEAException(e1.getMessage());
                }

                // Set S3 import path, preserving preparationSetName if present
                S3PathResolver s3ImportPathResolver = getImportS3PathForParquet(dataset, tableSchema, file);
                if (s3PathResolver.getPreparationSetCode() != null) {
                    s3ImportPathResolver.setPreparationSetCode(s3PathResolver.getPreparationSetCode());
                }

                String pathToS3ForImport = s3Helper.getS3Service().getS3Path(s3ImportPathResolver);
                String tablePathQuery = s3Helper.getS3Service().getTableAsFolderQueryPath(s3ImportPathResolver, getRightPath(dataset, true));

                s3Helper.uploadFileToBucket(pathToS3ForImport, parquetFile);
                dremioHelperService.refreshTableMetadataAndPromote(null, tablePathQuery, s3ImportPathResolver, tableSchema.getNameTableSchema());

            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }

        } catch (Exception ex) {
            LOG.error(ex.getMessage());
        } finally {
            dremioHelperService.deleteFileFromR3IfExists(parquetFile);
        }
    }

    /**
     * Extracted logic for creating fields and regenerating parquet tables.
     * This is now reused for both normal datasets and preparation datasets.
     */
    protected void createEmptyTableWithFields(DataSetMetabaseVO dataset, TableSchema tableSchema, S3PathResolver s3PathResolver) throws Exception {
        List<FieldSchema> fieldSchemas = new ArrayList<>(tableSchema.getRecordSchema().getFieldSchema());

        // Prepend mandatory fields
        FieldSchema recordIdSchema = new FieldSchema();
        recordIdSchema.setHeaderName("record_id");
        recordIdSchema.setType(DataType.TEXT);

        FieldSchema providerCodeSchema = new FieldSchema();
        providerCodeSchema.setHeaderName("data_provider_code");
        providerCodeSchema.setType(DataType.TEXT);

        fieldSchemas.add(0, providerCodeSchema);
        fieldSchemas.add(0, recordIdSchema);

        String currentField = "";
        try {
            List<Schema.Field> fields = new ArrayList<>();
            for (FieldSchema field : fieldSchemas) {
                currentField = field.getHeaderName();
                if (spatialDataHandling.getGeoJsonEnums().contains(field.getType())) {
                    fields.add(new Schema.Field(field.getHeaderName(), Schema.create(Schema.Type.BYTES)));
                } else {
                    fields.add(new Schema.Field(field.getHeaderName(), Schema.create(Schema.Type.STRING)));
                }
            }

            regenerateTable(dataset, tableSchema, fields, s3PathResolver);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains(ILLEGAL_CHAR_MARKER)) {
                throw new EEAException(EEAErrorMessage.ERROR_ILLEGAL_HEADER_CHARACTER + currentField);
            }
            throw e;
        }
    }

    @Override
    public void deleteTableIfEmpty(String tableSchemaName, S3PathResolver tablePathResolver) throws Exception {
        String tablePath = s3Helper.getS3Service().getTableAsFolderQueryPath(tablePathResolver, S3_TABLE_AS_FOLDER_QUERY_PATH);
        if (s3Helper.checkFolderExist(tablePathResolver, S3_TABLE_NAME_FOLDER_PATH) && !dremioHelperService.checkFolderPromoted(tablePathResolver, tablePathResolver.getTableName())) {
            dremioHelperService.promoteFolderOrFile(tablePathResolver, tablePathResolver.getTableName());
        }
        String numberOfRecordsQuery = "SELECT COUNT (*) FROM " + tablePath;
        if (s3Helper.checkFolderExist(tablePathResolver, S3_TABLE_NAME_FOLDER_PATH) && dremioJdbcTemplate.queryForObject(numberOfRecordsQuery, Long.class) == 0) {
            dremioHelperService.demoteFolderOrFile(tablePathResolver, tableSchemaName);
            s3Helper.deleteFolder(tablePathResolver, S3_TABLE_NAME_FOLDER_PATH);
        }
    }

    private S3PathResolver getImportS3PathForParquet(DataSetMetabaseVO dataset, TableSchema tableSchema, String parquetFilename) {
        S3PathResolver s3PathResolver = new S3PathResolver(dataset.getDataflowId(), dataset.getDataProviderId() != null ? dataset.getDataProviderId() : 0L, dataset.getId(), tableSchema.getNameTableSchema(), parquetFilename, getImportPathForParquet(dataset));
        s3PathResolver.setParquetFolder(tableSchema.getNameTableSchema() + "_" + UUID.randomUUID());
        return s3PathResolver;
    }

    private String getRightPath(DataSetMetabaseVO dataset, boolean isQueryPath) {
        if (Objects.requireNonNull(dataset.getDatasetTypeEnum()) == DatasetTypeEnum.REFERENCE) {
            return isQueryPath ? S3_DATAFLOW_REFERENCE_QUERY_PATH : S3_DATAFLOW_REFERENCE_FOLDER_PATH;
        }
        return isQueryPath ? S3_TABLE_AS_FOLDER_QUERY_PATH : S3_TABLE_NAME_FOLDER_PATH;
    }

    private String getImportPathForParquet(DataSetMetabaseVO dataset) {
        if (Objects.requireNonNull(dataset.getDatasetTypeEnum()) == DatasetTypeEnum.REFERENCE) {
            return S3_DATAFLOW_REFERENCE_PATH;
        }
        return S3_TABLE_NAME_WITH_PARQUET_FOLDER_PATH;
    }

    // ========================== Preparation Dataset Methods ==========================

    /**
     * Public method to run creation for a full preparation dataset.
     * Uses preparationSetName explicitly.
     */
    @Override
    public void runCreationForPreparationDataset(DataSetMetabaseVO parentDataset, String preparationSetName) throws EEAException {
        DataSetSchema schema = schemasRepository.findByIdDataSetSchema(new ObjectId(parentDataset.getDatasetSchema()));
        for (TableSchema tableSchema : schema.getTableSchemas()) {
            processTableSchemaForPreparationDataset(parentDataset, tableSchema, preparationSetName);
        }
    }

    /**
     * Public method to run creation for a specific table in a preparation dataset.
     */
    @Override
    public void runCreationForPreparationDatasetTable(DataSetMetabaseVO parentDataset, String tableSchemaId, String preparationSetName) throws EEAException {
        DataSetSchema schema = schemasRepository.findByIdDataSetSchema(new ObjectId(parentDataset.getDatasetSchema()));
        TableSchema targetTableSchema = schema.getTableSchemas().stream()
                .filter(table -> table.getIdTableSchema().toString().equals(tableSchemaId))
                .findFirst()
                .orElseThrow(() -> new EEAException("Table schema with ID " + tableSchemaId + " not found"));
        processTableSchemaForPreparationDataset(parentDataset, targetTableSchema, preparationSetName);
    }

    /**
     * Private method: process a table for a preparation dataset.
     * Key differences from normal table:
     * 1. preparationSetName is set on S3PathResolver
     * 2. Everything else (folder checks, field generation, parquet) is reused
     */
    private void processTableSchemaForPreparationDataset(DataSetMetabaseVO parentDataset, TableSchema tableSchema,
                                                         String preparationSetName) throws EEAException {
        S3PathResolver s3PathResolver = new S3PathResolver(
                parentDataset.getDataflowId(),
                parentDataset.getDataProviderId() != null ? parentDataset.getDataProviderId() : 0L,
                parentDataset.getId(),
                tableSchema.getNameTableSchema(),
                tableSchema.getNameTableSchema(),
                getRightPath(parentDataset, true)
        );

        // Set explicit preparation dataset name
        s3PathResolver.setPreparationSetCode(preparationSetName);

        try {
            LOG.info("[CHRIS] parentDataset.getDatasetTypeEnum().equals(DatasetTypeEnum.DESIGN)");
            if (parentDataset.getDatasetTypeEnum().equals(DatasetTypeEnum.DESIGN)) {
                LOG.info("[CHRIS] deleteTableIfEmpty");
                deleteTableIfEmpty(tableSchema.getNameTableSchema(), s3PathResolver);
            }

            LOG.info("[CHRIS] boolean folderExists");
            boolean folderExists = s3Helper.checkFolderExist(s3PathResolver, getRightPath(parentDataset, false));
            if (!folderExists) {
                LOG.info("[CHRIS] createEmptyTableWithFields");
                createEmptyTableWithFields(parentDataset, tableSchema, s3PathResolver);
            }
        } catch (EEAException eea) {
            throw eea;
        } catch (Exception e) {
            LOG.error("Error creating preparation dataset table for datasetId {}: {}", parentDataset.getId(), e.getMessage());
            throw new EEAException("Error creating preparation dataset table: " + e.getMessage());
        }
    }

}
