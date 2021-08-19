package org.eea.dataset.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.bson.types.ObjectId;
import org.eea.dataset.mapper.DesignDatasetMapper;
import org.eea.dataset.mapper.FieldSchemaNoRulesMapper;
import org.eea.dataset.mapper.TableSchemaMapper;
import org.eea.dataset.mapper.WebFormMapper;
import org.eea.dataset.persistence.metabase.repository.DesignDatasetRepository;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.ReferencedFieldSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
import org.eea.dataset.service.file.FileCommonUtils;
import org.eea.dataset.service.impl.DesignDatasetServiceImpl;
import org.eea.exception.EEAErrorMessage;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.ContributorController.ContributorControllerZuul;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetSchemaController;
import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZuul;
import org.eea.interfaces.controller.validation.RulesController.RulesControllerZuul;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.ReferencedFieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * The Class DesignDatasetServiceTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DesignDatasetServiceTest {

  @InjectMocks
  private DesignDatasetServiceImpl designDatasetService;

  @Mock
  private DesignDatasetRepository designDatasetRepository;

  @Mock
  private DesignDatasetMapper designDatasetMapper;

  @Mock
  private FileCommonUtils fileCommon;

  @Mock
  private LockService lockService;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private DatasetService datasetService;

  @Mock
  private DatasetSchemaService dataschemaService;

  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  @Mock
  private SchemasRepository schemasRepository;

  @Mock
  private TableSchemaMapper tableSchemaMapper;

  @Mock
  private FieldSchemaNoRulesMapper fieldSchemaNoRulesMapper;

  @Mock
  private DatasetSchemaController datasetSchemaController;

  @Mock
  private RulesControllerZuul rulesControllerZuul;

  @Mock
  private IntegrationControllerZuul integrationControllerZuul;

  @Mock
  private ContributorControllerZuul contributorControllerZuul;

  @Mock
  private RecordStoreControllerZuul recordStoreControllerZuul;

  @Mock
  private WebFormMapper webformMapper;

  private SecurityContext securityContext;

  private Authentication authentication;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    ReflectionTestUtils.setField(designDatasetService, "timeToWaitBeforeContinueCopy", 3000L);
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testGetDesignDataSetIdByDataflowIdNull() {
    when(designDatasetMapper.entityListToClass(Mockito.any())).thenReturn(new ArrayList<>());
    when(designDatasetRepository.findByDataflowId(Mockito.anyLong())).thenReturn(new ArrayList<>());
    assertEquals("failed assertion", new ArrayList<>(),
        designDatasetService.getDesignDataSetIdByDataflowId(Mockito.anyLong()));
  }

  @Test
  public void testGetDesignDataSetIdByDataflowId() {

    List<DesignDatasetVO> datasets = new ArrayList<>();
    DesignDatasetVO dataset = new DesignDatasetVO();
    dataset.setId(1L);
    datasets.add(dataset);
    List<Long> result = new ArrayList<>();
    result.add(1L);
    when(designDatasetRepository.findByDataflowId(Mockito.anyLong())).thenReturn(new ArrayList<>());
    when(designDatasetMapper.entityListToClass(Mockito.any())).thenReturn(datasets);
    assertEquals("failed assertion", datasets,
        designDatasetService.getDesignDataSetIdByDataflowId(Mockito.anyLong()));
  }

  @Test(expected = EEAException.class)
  public void testCopyDesignDatasetsException() throws EEAException {
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("user");
    when(designDatasetMapper.entityListToClass(Mockito.any())).thenReturn(null);
    try {
      designDatasetService.copyDesignDatasets(1L, 2L);
    } catch (ResponseStatusException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatus());
      throw e;
    }
  }

  @Test
  public void testCopyDesignDatasets() throws EEAException {

    DesignDatasetVO dataset = new DesignDatasetVO();
    dataset.setId(1L);
    dataset.setDatasetSchema("5ce524fad31fc52540abae73");
    dataset.setDataSetName("test");
    DataSetSchemaVO schemaVO = new DataSetSchemaVO();
    schemaVO.setIdDataSetSchema("5ce524fad31fc52540abae73");
    schemaVO.setNameDatasetSchema("test");
    TableSchemaVO tableVO = new TableSchemaVO();
    RecordSchemaVO recordVO = new RecordSchemaVO();
    FieldSchemaVO fieldSchemaVO = new FieldSchemaVO();
    fieldSchemaVO.setPkReferenced(true);
    fieldSchemaVO.setId("5ce524fad31fc52540abae73");
    fieldSchemaVO.setPk(true);
    fieldSchemaVO.setType(DataType.LINK);
    ReferencedFieldSchemaVO referenced = new ReferencedFieldSchemaVO();
    referenced.setIdDatasetSchema("5ce524fad31fc52540abae73");
    referenced.setIdPk("5ce524fad31fc52540abae73");
    fieldSchemaVO.setReferencedField(referenced);
    recordVO.setFieldSchema(Arrays.asList(fieldSchemaVO));
    recordVO.setIdRecordSchema("5ce524fad31fc52540abae73");
    tableVO.setRecordSchema(recordVO);
    tableVO.setIdTableSchema("5ce524fad31fc52540abae73");
    tableVO.setNameTableSchema("table1");
    schemaVO.setTableSchemas(Arrays.asList(tableVO));
    DataSetSchema schema = new DataSetSchema();
    TableSchema table = new TableSchema();
    RecordSchema record = new RecordSchema();
    FieldSchema field = new FieldSchema();
    ReferencedFieldSchema referenced2 = new ReferencedFieldSchema();
    referenced2.setIdDatasetSchema(new ObjectId("5ce524fad31fc52540abae73"));
    referenced2.setIdPk(new ObjectId("5ce524fad31fc52540abae73"));
    field.setIdFieldSchema(new ObjectId("5ce524fad31fc52540abae73"));
    field.setReferencedField(referenced2);
    field.setType(DataType.LINK);
    record.setFieldSchema(Arrays.asList(field));
    table.setRecordSchema(record);
    List<TableSchema> tableSchemas = new ArrayList<>();
    tableSchemas.add(table);
    schema.setTableSchemas(tableSchemas);

    when(dataschemaService.getDataSchemaById(Mockito.anyString())).thenReturn(schemaVO);
    when(dataschemaService.createEmptyDataSetSchema(Mockito.anyLong())).thenReturn(new ObjectId());
    when(datasetMetabaseService.createEmptyDataset(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(CompletableFuture.completedFuture(1L));
    when(schemasRepository.findByIdDataSetSchema(Mockito.any())).thenReturn(schema);
    Mockito.when(tableSchemaMapper.classToEntity(Mockito.any(TableSchemaVO.class)))
        .thenReturn(table);
    Mockito.when(fieldSchemaNoRulesMapper.classToEntity(Mockito.any(FieldSchemaVO.class)))
        .thenReturn(field);
    // when(designDatasetRepository.findByDataflowId(Mockito.anyLong())).thenReturn(Arrays.asList(dataset));
    when(designDatasetMapper.entityListToClass(Mockito.any())).thenReturn(Arrays.asList(dataset));
    Mockito.doNothing().when(recordStoreControllerZuul).createUpdateQueryView(Mockito.any(),
        Mockito.anyBoolean());

    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");
    designDatasetService.copyDesignDatasets(1L, 2L);
    Mockito.verify(datasetMetabaseService, times(1)).createEmptyDataset(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test(expected = EEAException.class)
  public void copyDesignDatasetsExceptionTest() throws EEAException {
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
    Mockito.when(authentication.getName()).thenReturn("name");
    Mockito.when(designDatasetRepository.findByDataflowId(Mockito.anyLong()))
        .thenReturn(new ArrayList<>());
    Mockito.when(designDatasetMapper.entityListToClass(Mockito.any()))
        .thenReturn(Arrays.asList(new DesignDatasetVO()));
    Mockito.when(dataschemaService.createEmptyDataSetSchema(Mockito.anyLong()))
        .thenThrow(EEAException.class);
    try {
      designDatasetService.copyDesignDatasets(1L, 2L);
    } catch (EEAException e) {
      Assert.assertEquals(String.format(EEAErrorMessage.ERROR_COPYING_SCHEMAS, 1L, 2L),
          e.getMessage());
      throw e;
    }
  }
}
