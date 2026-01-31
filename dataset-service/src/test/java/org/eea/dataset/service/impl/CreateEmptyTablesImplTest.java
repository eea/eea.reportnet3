package org.eea.dataset.service.impl;

import org.bson.types.ObjectId;
import org.eea.datalake.service.DremioHelperService;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.S3Service;
import org.eea.datalake.service.SpatialDataHandling;
import org.eea.datalake.service.model.S3PathResolver;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.eea.utils.LiteralConstants.*;

/**
 * Test class for CreateEmptyTablesImpl.
 */
public class CreateEmptyTablesImplTest {

    @InjectMocks
    private CreateEmptyTablesImpl createEmptyTables;

    private CreateEmptyTablesImpl createEmptyTablesSpy;

    @Mock
    private S3Helper s3Helper;

    @Mock
    private DremioHelperService dremioHelperService;

    @Mock
    private SpatialDataHandling spatialDataHandling;

    @Mock
    private SchemasRepository schemasRepository;

    @Mock
    private JdbcTemplate dremioJdbcTemplate;

    @Mock
    private S3Service s3Service;

    @Before
    public void initMocks() {
        MockitoAnnotations.openMocks(this);

        // IMPORTANT:
        // We spy the service to disable heavy side-effect methods
        // (filesystem, parquet writer, S3 upload, etc.)
        createEmptyTablesSpy = Mockito.spy(createEmptyTables);

        ReflectionTestUtils.setField(createEmptyTablesSpy, "parquetFilePath", "/tmp/");
    }

    // ============================================================================
    // Normal dataset tests
    // ============================================================================

    @Test
    public void testRunCreationForOneDataset_createsTablesWhenFolderDoesNotExist1() throws Exception {

        // GIVEN
        DataSetMetabaseVO dataset = new DataSetMetabaseVO();
        dataset.setId(1L);
        dataset.setDataflowId(10L);
        dataset.setDataProviderId(20L);
        dataset.setDatasetSchema("507f1f77bcf86cd799439011");
        // Using REFERENCE to avoid trigger for DESIGN cleanup logic `deleteTableIfEmpty`
        // This test should only verify table creation when the folder doesn't exist
        dataset.setDatasetTypeEnum(DatasetTypeEnum.REFERENCE);

        FieldSchema fieldSchema = new FieldSchema();
        fieldSchema.setHeaderName("test_column");
        fieldSchema.setType(DataType.TEXT);

        RecordSchema recordSchema = new RecordSchema();
        recordSchema.setFieldSchema(List.of(fieldSchema));

        TableSchema tableSchema = new TableSchema();
        tableSchema.setNameTableSchema("my_table");
        tableSchema.setRecordSchema(recordSchema);

        DataSetSchema dataSetSchema = new DataSetSchema();
        dataSetSchema.setTableSchemas(List.of(tableSchema));

        Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any(ObjectId.class)))
                .thenReturn(dataSetSchema);

        Mockito.when(s3Helper.checkFolderExist(Mockito.any(), Mockito.anyString()))
                .thenReturn(false);

        Mockito.when(spatialDataHandling.getGeoJsonEnums())
                .thenReturn(Collections.emptyList());

        // CRITICAL: disable heavy side-effects
        Mockito.doNothing()
                .when(createEmptyTablesSpy)
                .regenerateTable(
                        Mockito.any(),
                        Mockito.any(),
                        Mockito.anyList(),
                        Mockito.any()
                );

        // WHEN
        createEmptyTablesSpy.runCreationForOneDataset(dataset);

        // THEN
        Mockito.verify(schemasRepository, times(1))
                .findByIdDataSetSchema(Mockito.any(ObjectId.class));

        Mockito.verify(s3Helper, Mockito.atLeastOnce())
                .checkFolderExist(Mockito.any(), Mockito.anyString());

        Mockito.verify(createEmptyTablesSpy, times(1))
                .regenerateTable(
                        Mockito.any(DataSetMetabaseVO.class),
                        Mockito.any(TableSchema.class),
                        Mockito.anyList(),
                        Mockito.any(S3PathResolver.class)
                );
    }

    @Test
    public void testRunCreationForOneDataset_doesNothingWhenFolderExists() throws Exception {
        // GIVEN
        DataSetMetabaseVO dataSetMetabaseVO = new DataSetMetabaseVO();
        dataSetMetabaseVO.setId(1L);
        dataSetMetabaseVO.setDataflowId(10L);
        dataSetMetabaseVO.setDatasetSchema("507f1f77bcf86cd799439011");
        dataSetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.REFERENCE);

        TableSchema tableSchema = new TableSchema();
        tableSchema.setNameTableSchema("table");
        tableSchema.setRecordSchema(new RecordSchema());

        DataSetSchema dataSetSchema = new DataSetSchema();
        dataSetSchema.setTableSchemas(List.of(tableSchema));

        Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any()))
                .thenReturn(dataSetSchema);

        Mockito.when(s3Helper.checkFolderExist(Mockito.any(), Mockito.anyString()))
                .thenReturn(true);

        // WHEN
        createEmptyTablesSpy.runCreationForOneDataset(dataSetMetabaseVO);

        // THEN
        Mockito.verify(createEmptyTablesSpy, Mockito.never())
                .createEmptyTableWithFields(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test(expected = EEAException.class)
    public void testRunCreationForSpecificTableSchema_throwsWhenTableSchemaNotFound() throws Exception {
        // GIVEN
        DataSetMetabaseVO dataSetMetabaseVO = new DataSetMetabaseVO();
        dataSetMetabaseVO.setDatasetSchema("507f1f77bcf86cd799439011");

        DataSetSchema dataSetSchema = new DataSetSchema();
        dataSetSchema.setTableSchemas(Collections.emptyList());

        Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any()))
                .thenReturn(dataSetSchema);

        // WHEN
        createEmptyTablesSpy.runCreationForSpecificTableSchema(dataSetMetabaseVO, "missing-id");

        // THEN throw exception
    }

    // ============================================================================
    // Preparation dataset tests
    // ============================================================================

    @Test
    public void testRunCreationForPreparationDataset_setsPreparationSetNameOnS3PathResolver() throws Exception {
        // GIVEN
        DataSetMetabaseVO parentDataSetMetabaseVO = new DataSetMetabaseVO();
        parentDataSetMetabaseVO.setId(1L);
        parentDataSetMetabaseVO.setDataflowId(10L);
        parentDataSetMetabaseVO.setDatasetSchema("507f1f77bcf86cd799439011");
        parentDataSetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.REFERENCE);

        TableSchema tableSchema = new TableSchema();
        tableSchema.setNameTableSchema("table");
        tableSchema.setRecordSchema(new RecordSchema());

        DataSetSchema dataSetSchema = new DataSetSchema();
        dataSetSchema.setTableSchemas(List.of(tableSchema));

        Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any()))
                .thenReturn(dataSetSchema);

        Mockito.when(s3Helper.checkFolderExist(Mockito.any(), Mockito.anyString()))
                .thenReturn(false);

        Mockito.doNothing()
                .when(createEmptyTablesSpy)
                .createEmptyTableWithFields(Mockito.any(), Mockito.any(), Mockito.any());

        // WHEN
        createEmptyTablesSpy.runCreationForPreparationDataset(
                parentDataSetMetabaseVO,
                "section_a_granada"
        );

        // THEN
        Mockito.verify(createEmptyTablesSpy)
                .createEmptyTableWithFields(
                        Mockito.eq(parentDataSetMetabaseVO),
                        Mockito.eq(tableSchema),
                        Mockito.argThat(resolver ->
                                "section_a_granada".equals(resolver.getPreparationSetCode())
                        )
                );
    }

    @Test
    public void testRunCreationForPreparationDatasetTable_onlyProcessesSingleTable() throws Exception {
        // GIVEN
        DataSetMetabaseVO dataSetMetabaseVO = new DataSetMetabaseVO();
        dataSetMetabaseVO.setId(1L);
        dataSetMetabaseVO.setDataflowId(10L);
        dataSetMetabaseVO.setDatasetSchema("507f1f77bcf86cd799439011");
        dataSetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.REFERENCE);

        TableSchema tableSchema1 = new TableSchema();
        tableSchema1.setIdTableSchema(new ObjectId());
        tableSchema1.setNameTableSchema("t1");
        tableSchema1.setRecordSchema(new RecordSchema());

        TableSchema tableSchema2 = new TableSchema();
        tableSchema2.setIdTableSchema(new ObjectId());
        tableSchema2.setNameTableSchema("t2");
        tableSchema2.setRecordSchema(new RecordSchema());

        DataSetSchema dataSetSchema = new DataSetSchema();
        dataSetSchema.setTableSchemas(List.of(tableSchema1, tableSchema2));

        Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any()))
                .thenReturn(dataSetSchema);

        Mockito.when(s3Helper.checkFolderExist(Mockito.any(), Mockito.anyString()))
                .thenReturn(false);

        Mockito.doNothing()
                .when(createEmptyTablesSpy)
                .createEmptyTableWithFields(Mockito.any(), Mockito.any(), Mockito.any());

        // WHEN
        createEmptyTablesSpy.runCreationForPreparationDatasetTable(
                dataSetMetabaseVO,
                tableSchema1.getIdTableSchema().toString(),
                "prep"
        );

        // THEN
        Mockito.verify(createEmptyTablesSpy, Mockito.times(1))
                .createEmptyTableWithFields(
                        Mockito.eq(dataSetMetabaseVO),
                        Mockito.eq(tableSchema1),
                        Mockito.any()
                );
    }

    // ============================================================================
    // DESIGN dataset cleanup tests
    // ============================================================================

    @Test
    public void testDeleteTableIfEmpty_demotesAndDeletesWhenEmpty() throws Exception {
        // GIVEN
        S3PathResolver tablePathResolver = Mockito.mock(S3PathResolver.class);
        Mockito.when(tablePathResolver.getTableName()).thenReturn("table");

        // Ensure s3Helper.getS3Service() returns a mock
        S3Service s3ServiceMock = Mockito.mock(S3Service.class);
        Mockito.when(s3Helper.getS3Service()).thenReturn(s3ServiceMock);
        Mockito.when(s3ServiceMock.getTableAsFolderQueryPath(Mockito.any(), Mockito.anyString()))
                .thenReturn("fake/table/path");

        // Folder exists
        Mockito.when(s3Helper.checkFolderExist(Mockito.eq(tablePathResolver), Mockito.eq(S3_TABLE_NAME_FOLDER_PATH)))
                .thenReturn(true);

        // Table promoted
        Mockito.when(dremioHelperService.checkFolderPromoted(Mockito.eq(tablePathResolver), Mockito.eq("table")))
                .thenReturn(true);

        // Zero rows → triggers deletion
        Mockito.when(dremioJdbcTemplate.queryForObject(Mockito.anyString(), Mockito.eq(Long.class)))
                .thenReturn(0L);

        // Inject mocks into class under test to avoid spy issues
        ReflectionTestUtils.setField(createEmptyTables, "s3Helper", s3Helper);
        ReflectionTestUtils.setField(createEmptyTables, "dremioHelperService", dremioHelperService);
        ReflectionTestUtils.setField(createEmptyTables, "dremioJdbcTemplate", dremioJdbcTemplate);

        // WHEN
        createEmptyTables.deleteTableIfEmpty("table", tablePathResolver);

        // THEN
        Mockito.verify(dremioHelperService, Mockito.times(1))
                .demoteFolderOrFile(tablePathResolver, "table");

        Mockito.verify(s3Helper, Mockito.times(1))
                .deleteFolder(tablePathResolver, S3_TABLE_NAME_FOLDER_PATH);
    }

    @Test
    public void testDeleteTableIfEmpty_doesNothingWhenNotEmpty_fixed() throws Exception {
        // GIVEN
        S3PathResolver tablePathResolver = Mockito.mock(S3PathResolver.class);
        Mockito.when(tablePathResolver.getTableName()).thenReturn("table");

        // Mock S3Service
        S3Service s3ServiceMock = Mockito.mock(S3Service.class);
        Mockito.when(s3Helper.getS3Service()).thenReturn(s3ServiceMock);
        Mockito.when(s3ServiceMock.getTableAsFolderQueryPath(Mockito.any(), Mockito.anyString()))
                .thenReturn("fake/table/path");

        // Folder exists
        Mockito.when(s3Helper.checkFolderExist(Mockito.eq(tablePathResolver), Mockito.eq(S3_TABLE_NAME_FOLDER_PATH)))
                .thenReturn(true);

        // Table has >0 rows → should do nothing
        Mockito.when(dremioJdbcTemplate.queryForObject(Mockito.anyString(), Mockito.eq(Long.class)))
                .thenReturn(5L);

        // Inject mocks to avoid spy/reference issues
        ReflectionTestUtils.setField(createEmptyTables, "s3Helper", s3Helper);
        ReflectionTestUtils.setField(createEmptyTables, "dremioHelperService", dremioHelperService);
        ReflectionTestUtils.setField(createEmptyTables, "dremioJdbcTemplate", dremioJdbcTemplate);

        // WHEN
        createEmptyTables.deleteTableIfEmpty("table", tablePathResolver);

        // THEN
        Mockito.verify(dremioHelperService, Mockito.never())
                .demoteFolderOrFile(Mockito.any(), Mockito.any());

        Mockito.verify(s3Helper, Mockito.never())
                .deleteFolder(Mockito.any(), Mockito.anyString());
    }

    // ============================================================================
    // Error handling tests
    // ============================================================================

    @Test(expected = EEAException.class)
    public void testIllegalCharacterInFieldHeaderThrowsEEAException() throws Exception {
        // GIVEN
        DataSetMetabaseVO dataSetMetabaseVO = new DataSetMetabaseVO();
        dataSetMetabaseVO.setId(1L);
        dataSetMetabaseVO.setDataflowId(10L);
        dataSetMetabaseVO.setDatasetSchema("507f1f77bcf86cd799439011");
        dataSetMetabaseVO.setDatasetTypeEnum(DatasetTypeEnum.REFERENCE);

        FieldSchema fieldSchema = new FieldSchema();
        fieldSchema.setHeaderName("bad@header");
        fieldSchema.setType(DataType.TEXT);

        RecordSchema recordSchema = new RecordSchema();
        recordSchema.setFieldSchema(List.of(fieldSchema));

        TableSchema tableSchema = new TableSchema();
        tableSchema.setNameTableSchema("table");
        tableSchema.setRecordSchema(recordSchema);

        DataSetSchema dataSetSchema = new DataSetSchema();
        dataSetSchema.setTableSchemas(List.of(tableSchema));

        Mockito.when(schemasRepository.findByIdDataSetSchema(Mockito.any()))
                .thenReturn(dataSetSchema);

        Mockito.when(spatialDataHandling.getGeoJsonEnums())
                .thenReturn(Collections.emptyList());

        Mockito.doThrow(new RuntimeException("Illegal character in"))
                .when(createEmptyTablesSpy)
                .regenerateTable(Mockito.any(), Mockito.any(), Mockito.anyList(), Mockito.any());

        // WHEN
        createEmptyTablesSpy.runCreationForOneDataset(dataSetMetabaseVO);

        // THEN -> exception
    }
}
