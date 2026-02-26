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

        List<FieldSchema> fieldSchemas = tableSchema.getRecordSchema().getFieldSchema();

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

          regenerateTables(dataset, tableSchema, fields);
          LOG.info("Created empty table for dataflowId {} providerId {} datasetId {} and table {}", s3TablePathResolver.getDataflowId(), s3TablePathResolver.getDataProviderId(),
                  s3TablePathResolver.getDatasetId(), s3TablePathResolver.getTableName());
        } catch (Exception e) {
          String msg = e.getMessage();
          if (msg != null && msg.contains(ILLEGAL_CHAR_MARKER)) {
            throw new EEAException(EEAErrorMessage.ERROR_ILLEGAL_HEADER_CHARACTER + currentField);
          }
          throw e;
        }
      }
    } catch (EEAException eea) {
      throw eea;
    } catch (Exception e) {
      LOG.error("Something went wrong, trying to create empty table for dataflowId {} providerId {} datasetId {} and table {} , with exception message: {}",
              dataset.getDataflowId(), dataset.getDataProviderId(), dataset.getId(), tableSchema.getNameTableSchema(), e.getMessage());
      throw new EEAException("Something went wrong, trying to create empty tables with message: " + e.getMessage());
    }
  }

  private void regenerateTables(DataSetMetabaseVO dataset, TableSchema tableSchema, List<Schema.Field> fields) throws Exception {
    Schema schema1 = Schema.createRecord("Data", null, null, false, fields);
    String file = "0_0_0.parquet";
    String parquetFile = parquetFilePath + UUID.randomUUID() + "/" + file;
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

      S3PathResolver s3PathResolver = getImportS3PathForParquet(dataset, tableSchema, file);
      String pathToS3ForImport = s3Helper.getS3Service().getS3Path(s3PathResolver);
      String tablePath1 = s3Helper.getS3Service().getTableAsFolderQueryPath(s3PathResolver, getRightPath(dataset, true));

      s3Helper.uploadFileToBucket(pathToS3ForImport, parquetFile);
      dremioHelperService.refreshTableMetadataAndPromote(null, tablePath1, s3PathResolver, tableSchema.getNameTableSchema());
    } catch (Exception ex) {
      LOG.error(ex.getMessage());
    } finally {
      dremioHelperService.deleteFileFromR3IfExists(parquetFile);
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

}
