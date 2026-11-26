package org.eea.dataset.persistence.data.repository;

import static org.eea.utils.LiteralConstants.S3_TABLE_AS_FOLDER_QUERY_PATH;
import static org.eea.utils.LiteralConstants.S3_TABLE_NAME_EU_QUERY_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The Class RecordRepositoryImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class RecordRepositoryImplTest {

  /** The Constant DATASET_ID. */
  private static final Long DATASET_ID = 43L;

  /** The Constant DATAFLOW_ID. */
  private static final Long DATAFLOW_ID = 13L;

  /** The Constant TABLE_NAME. */
  private static final String TABLE_NAME = "tb1";

  /** The Constant DREMIO_TABLE_PATH. */
  private static final String DREMIO_TABLE_PATH = "\"rn3-dataset\".\"rn3-dataset\".\"table\"";

  /** The record repository impl. */
  @InjectMocks
  private RecordRepositoryImpl recordRepositoryImpl;

  /** The dataset metabase service. */
  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  /** The schemas repository. */
  @Mock
  private SchemasRepository schemasRepository;

  /** The s3 service. */
  @Mock
  private S3Service s3Service;

  /** The dremio jdbc template. */
  @Mock
  private JdbcTemplate dremioJdbcTemplate;

  /** The temporary folder. */
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  /** The json file. */
  private File jsonFile;

  /** The only table of the dataset schema. */
  private TableSchema tableSchema;

  /**
   * Inits the mocks.
   *
   * @throws Exception the exception
   */
  @Before
  public void initMocks() throws Exception {
    ObjectId datasetSchemaId = new ObjectId();
    tableSchema = new TableSchema();
    tableSchema.setIdTableSchema(new ObjectId());
    tableSchema.setNameTableSchema(TABLE_NAME);
    DataSetSchema dataSetSchema = new DataSetSchema();
    dataSetSchema.setTableSchemas(Collections.singletonList(tableSchema));

    when(datasetMetabaseService.findDatasetSchemaIdById(DATASET_ID))
        .thenReturn(datasetSchemaId.toString());
    when(schemasRepository.findById(datasetSchemaId)).thenReturn(Optional.of(dataSetSchema));
    when(dremioJdbcTemplate.queryForObject(Mockito.anyString(), ArgumentMatchers.eq(Long.class)))
        .thenReturn(0L);

    jsonFile = temporaryFolder.newFile("etlExport.json");
  }

  /**
   * Find and generate ETL json DL test dataset test.
   *
   * @throws Exception the exception
   */
  @Test
  public void findAndGenerateETLJsonDLTestDatasetTest() throws Exception {
    when(datasetMetabaseService.findDatasetMetabase(DATASET_ID))
        .thenReturn(datasetMetabase(DatasetTypeEnum.TEST));
    when(s3Service.getS3Path(Mockito.any(S3PathResolver.class))).thenReturn(DREMIO_TABLE_PATH);

    recordRepositoryImpl.findAndGenerateETLJsonDL(DATASET_ID, null, null, null, null, null, null,
        jsonFile, null);

    ArgumentCaptor<S3PathResolver> captor = ArgumentCaptor.forClass(S3PathResolver.class);
    verify(s3Service).getS3Path(captor.capture());
    assertEquals(S3_TABLE_AS_FOLDER_QUERY_PATH, captor.getValue().getPath());
    assertEquals(0L, captor.getValue().getDataProviderId());
    assertEquals(DATASET_ID.longValue(), captor.getValue().getDatasetId());
    assertEquals(TABLE_NAME, captor.getValue().getTableName());
    verify(dremioJdbcTemplate).queryForObject("select count(record_id) from " + DREMIO_TABLE_PATH,
        Long.class);
    assertTrue(readJsonFile().contains("\"tableName\":\"" + TABLE_NAME + "\""));
  }

  /**
   * Find and generate ETL json DL EU dataset test.
   *
   * @throws Exception the exception
   */
  @Test
  public void findAndGenerateETLJsonDLEUDatasetTest() throws Exception {
    when(datasetMetabaseService.findDatasetMetabase(DATASET_ID))
        .thenReturn(datasetMetabase(DatasetTypeEnum.EUDATASET));
    when(s3Service.getTableDCAsFolderQueryPath(Mockito.any(S3PathResolver.class),
        ArgumentMatchers.eq(S3_TABLE_NAME_EU_QUERY_PATH))).thenReturn(DREMIO_TABLE_PATH);

    recordRepositoryImpl.findAndGenerateETLJsonDL(DATASET_ID, null, null, null, null, null, null,
        jsonFile, null);

    ArgumentCaptor<S3PathResolver> captor = ArgumentCaptor.forClass(S3PathResolver.class);
    verify(s3Service).getTableDCAsFolderQueryPath(captor.capture(),
        ArgumentMatchers.eq(S3_TABLE_NAME_EU_QUERY_PATH));
    assertEquals(DATASET_ID.longValue(), captor.getValue().getDatasetId());
    assertEquals(DATAFLOW_ID.longValue(), captor.getValue().getDataflowId());
    assertEquals(TABLE_NAME, captor.getValue().getTableName());
    verify(s3Service, never()).getS3Path(Mockito.any(S3PathResolver.class));
    verify(dremioJdbcTemplate).queryForObject("select count(record_id) from " + DREMIO_TABLE_PATH,
        Long.class);
    assertTrue(readJsonFile().contains("\"tableName\":\"" + TABLE_NAME + "\""));
  }

  /**
   * Table without the filter column: no Dremio query, the table is written with 0 records.
   *
   * @throws Exception the exception
   */
  @Test
  public void findAndGenerateETLJsonDLColumnMissingSkipsDremioTest() throws Exception {
    withFields("reportingYear");

    recordRepositoryImpl.findAndGenerateETLJsonDL(DATASET_ID, null, null, null, "SE",
        "countryCode", null, jsonFile, null);

    verify(dremioJdbcTemplate, never()).queryForObject(Mockito.anyString(),
        ArgumentMatchers.eq(Long.class));
    verify(datasetMetabaseService, never()).findDatasetMetabase(Mockito.anyLong());
    String json = readJsonFile();
    assertTrue(json.contains("\"tableName\":\"" + TABLE_NAME + "\""));
    assertTrue(json.contains("\"totalRecords\":0"));
  }

  /**
   * Table with the filter column (case-insensitive match): Dremio is queried with the filter.
   *
   * @throws Exception the exception
   */
  @Test
  public void findAndGenerateETLJsonDLColumnPresentQueriesDremioTest() throws Exception {
    withFields("countryCode");
    when(datasetMetabaseService.findDatasetMetabase(DATASET_ID))
        .thenReturn(datasetMetabase(DatasetTypeEnum.TEST));
    when(s3Service.getS3Path(Mockito.any(S3PathResolver.class))).thenReturn(DREMIO_TABLE_PATH);

    recordRepositoryImpl.findAndGenerateETLJsonDL(DATASET_ID, null, null, null, "SE",
        "COUNTRYCODE", null, jsonFile, null);

    verify(dremioJdbcTemplate).queryForObject(
        ArgumentMatchers.contains("where COUNTRYCODE = 'SE'"), ArgumentMatchers.eq(Long.class));
  }

  /**
   * columnName without filterValue: no filter is applied, so the table is queried as before.
   *
   * @throws Exception the exception
   */
  @Test
  public void findAndGenerateETLJsonDLColumnNameWithoutFilterValueNotSkippedTest()
      throws Exception {
    when(datasetMetabaseService.findDatasetMetabase(DATASET_ID))
        .thenReturn(datasetMetabase(DatasetTypeEnum.TEST));
    when(s3Service.getS3Path(Mockito.any(S3PathResolver.class))).thenReturn(DREMIO_TABLE_PATH);

    recordRepositoryImpl.findAndGenerateETLJsonDL(DATASET_ID, null, null, null, null, "xyz",
        null, jsonFile, null);

    verify(dremioJdbcTemplate).queryForObject("select count(record_id) from " + DREMIO_TABLE_PATH,
        Long.class);
  }

  /**
   * Dremio count error: the table is written with 0 records and the export goes on.
   *
   * @throws Exception the exception
   */
  @Test
  public void findAndGenerateETLJsonDLDremioErrorReturnsZeroRecordsTest() throws Exception {
    withFields("countryCode");
    when(datasetMetabaseService.findDatasetMetabase(DATASET_ID))
        .thenReturn(datasetMetabase(DatasetTypeEnum.TEST));
    when(s3Service.getS3Path(Mockito.any(S3PathResolver.class))).thenReturn(DREMIO_TABLE_PATH);
    Mockito.doThrow(new UncategorizedSQLException("count", "select",
        new SQLException("VALIDATION ERROR"))).when(dremioJdbcTemplate)
        .queryForObject(Mockito.anyString(), ArgumentMatchers.eq(Long.class));

    recordRepositoryImpl.findAndGenerateETLJsonDL(DATASET_ID, null, null, null, "SE",
        "countryCode", null, jsonFile, null);

    String json = readJsonFile();
    assertTrue(json.contains("\"tableName\":\"" + TABLE_NAME + "\""));
    assertTrue(json.contains("\"totalRecords\":0"));
  }

  /**
   * Gives the table of the dataset schema the given field names.
   *
   * @param headerNames the header names
   */
  private void withFields(String... headerNames) {
    List<FieldSchema> fields = new ArrayList<>();
    for (String headerName : headerNames) {
      FieldSchema field = new FieldSchema();
      field.setIdFieldSchema(new ObjectId());
      field.setHeaderName(headerName);
      fields.add(field);
    }
    RecordSchema recordSchema = new RecordSchema();
    recordSchema.setFieldSchema(fields);
    tableSchema.setRecordSchema(recordSchema);
  }

  /**
   * Dataset metabase.
   *
   * @param datasetType the dataset type
   * @return the data set metabase VO
   */
  private DataSetMetabaseVO datasetMetabase(DatasetTypeEnum datasetType) {
    DataSetMetabaseVO dataset = new DataSetMetabaseVO();
    dataset.setId(DATASET_ID);
    dataset.setDataflowId(DATAFLOW_ID);
    dataset.setDatasetTypeEnum(datasetType);
    return dataset;
  }

  /**
   * Read json file.
   *
   * @return the string
   * @throws Exception the exception
   */
  private String readJsonFile() throws Exception {
    return new String(Files.readAllBytes(jsonFile.toPath()), StandardCharsets.UTF_8);
  }
}
