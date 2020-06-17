package org.eea.dataset.service.helper;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.eea.dataset.mapper.DataSetMapper;
import org.eea.dataset.persistence.data.domain.DatasetValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.dataset.persistence.data.domain.TableValue;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.interfaces.vo.dataflow.DataProviderVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.lock.service.LockService;
import org.eea.notification.event.NotificableEventHandler;
import org.eea.notification.factory.NotificableEventFactory;
import org.eea.thread.ThreadPropertiesManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FileTreatmentHelperTest {

  @InjectMocks
  private FileTreatmentHelper fileTreatmentHelper;

  @Mock
  private DatasetService datasetService;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private NotificableEventFactory notificableEventFactory;

  @Mock
  private LockService lockService;

  @Mock
  private DataSetMapper dataSetMapper;

  @Mock
  private DataSetVO datasetVO;

  @Mock
  private DatasetValue datasetValue;

  @Mock
  private List<TableValue> listTableValue;

  @Mock
  private TableValue tableValue;

  @Mock
  private List<RecordValue> listRecordValue;

  @Mock
  private Stream<TableValue> tableValueStream;

  @Mock
  private NotificableEventHandler notificableEventHandler;

  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  @Mock
  private RepresentativeControllerZuul representativeControllerZuul;

  @Mock
  private DatasetSchemaService datasetSchemaService;

  @Mock
  private IntegrationControllerZuul integrationController;

  List<IntegrationVO> integrations;

  IntegrationVO integrationVO;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    ThreadPropertiesManager.setVariable("name", "user");
    integrationVO = new IntegrationVO();
    integrations = new ArrayList<>();
    integrations.add(integrationVO);
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void executeFileProcessTest1() throws EEAException, IOException {
    Mockito
        .when(
            datasetService.processFile(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(datasetVO);
    Mockito.doNothing().when(datasetVO).setId(Mockito.any());
    Mockito.when(dataSetMapper.classToEntity(Mockito.any())).thenReturn(datasetValue);
    Mockito.when(datasetValue.getTableValues()).thenReturn(listTableValue);
    Mockito.when(listTableValue.get(Mockito.anyInt())).thenReturn(tableValue);
    Mockito.when(tableValue.getRecords()).thenReturn(listRecordValue);
    Mockito.doNothing().when(tableValue).setRecords(Mockito.any());
    Mockito.when(datasetService.findTableIdByTableSchema(Mockito.any(), Mockito.any()))
        .thenReturn(1L);
    Mockito.when(listTableValue.stream()).thenReturn(tableValueStream);
    Mockito.when(tableValueStream.filter(Mockito.any())).thenReturn(tableValueStream);
    Mockito.doNothing().when(tableValueStream).forEach(Mockito.any());
    Mockito.when(listRecordValue.size()).thenReturn(2000);
    Mockito.when(listRecordValue.subList(Mockito.anyInt(), Mockito.anyInt()))
        .thenReturn(new ArrayList<RecordValue>());
    Mockito.doNothing().when(datasetService).saveAllRecords(Mockito.any(), Mockito.any());
    when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
    when(representativeControllerZuul.findDataProviderById(Mockito.any()))
        .thenReturn(new DataProviderVO());
    Mockito.doNothing().when(kafkaSenderUtils).releaseDatasetKafkaEvent(Mockito.any(),
        Mockito.any());
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
    Mockito.when(lockService.removeLockByCriteria(Mockito.any())).thenReturn(true);
    Mockito.when(datasetService.getMimetype(Mockito.any())).thenReturn("xls");
    Mockito.when(datasetSchemaService.getDatasetSchemaId(Mockito.any())).thenReturn("123456");
    Mockito.when(integrationController.findAllIntegrationsByCriteria(Mockito.any()))
        .thenReturn(integrations);
    fileTreatmentHelper.executeFileProcess(1L, "fileName", new ByteArrayInputStream(new byte[0]),
        "5d4abe555b1c1e0001477410");
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void executeFileProcessTest2() throws EEAException, IOException {
    Mockito
        .when(
            datasetService.processFile(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(datasetVO);
    Mockito.doNothing().when(datasetVO).setId(Mockito.any());
    Mockito.when(dataSetMapper.classToEntity(Mockito.any())).thenReturn(datasetValue);
    Mockito.when(datasetValue.getTableValues()).thenReturn(listTableValue);
    Mockito.when(listTableValue.get(Mockito.anyInt())).thenReturn(tableValue);
    Mockito.when(tableValue.getRecords()).thenReturn(listRecordValue);
    Mockito.doNothing().when(tableValue).setRecords(Mockito.any());
    Mockito.when(datasetService.findTableIdByTableSchema(Mockito.any(), Mockito.any()))
        .thenReturn(null);
    Mockito.doNothing().when(datasetService).saveTable(Mockito.any(), Mockito.any());
    Mockito.when(listRecordValue.size()).thenReturn(1);
    Mockito.when(listRecordValue.subList(Mockito.anyInt(), Mockito.anyInt()))
        .thenReturn(new ArrayList<RecordValue>());
    Mockito.doNothing().when(datasetService).saveAllRecords(Mockito.any(), Mockito.any());
    Mockito.doNothing().when(kafkaSenderUtils).releaseDatasetKafkaEvent(Mockito.any(),
        Mockito.any());
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
    when(datasetMetabaseService.findDatasetMetabase(Mockito.anyLong()))
        .thenReturn(new DataSetMetabaseVO());
    when(representativeControllerZuul.findDataProviderById(Mockito.any()))
        .thenReturn(new DataProviderVO());
    Mockito.when(lockService.removeLockByCriteria(Mockito.any())).thenReturn(true);
    Mockito.when(datasetService.getMimetype(Mockito.any())).thenReturn("xls");
    Mockito.when(datasetSchemaService.getDatasetSchemaId(Mockito.any())).thenReturn("123456");
    Mockito.when(integrationController.findAllIntegrationsByCriteria(Mockito.any()))
        .thenReturn(integrations);
    fileTreatmentHelper.executeFileProcess(1L, "fileName", new ByteArrayInputStream(new byte[0]),
        "5d4abe555b1c1e0001477410");
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }

  @Test
  public void executeFileProcessTest3() throws EEAException, IOException {
    Mockito
        .when(
            datasetService.processFile(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(datasetVO);
    Mockito.doNothing().when(datasetVO).setId(Mockito.any());
    Mockito.when(dataSetMapper.classToEntity(Mockito.any())).thenReturn(null);
    Mockito.doNothing().when(kafkaSenderUtils).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
    Mockito.when(lockService.removeLockByCriteria(Mockito.any())).thenReturn(true);
    Mockito.when(datasetService.getMimetype(Mockito.any())).thenReturn("xls");
    Mockito.when(datasetSchemaService.getDatasetSchemaId(Mockito.any())).thenReturn("123456");
    Mockito.when(integrationController.findAllIntegrationsByCriteria(Mockito.any()))
        .thenReturn(integrations);
    fileTreatmentHelper.executeFileProcess(1L, "fileName", new ByteArrayInputStream(new byte[0]),
        "5d4abe555b1c1e0001477410");
    Mockito.verify(kafkaSenderUtils, times(1)).releaseNotificableKafkaEvent(Mockito.any(),
        Mockito.any(), Mockito.any());
  }
}
