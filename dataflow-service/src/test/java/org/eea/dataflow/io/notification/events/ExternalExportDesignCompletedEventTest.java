package org.eea.dataflow.io.notification.events;

import org.eea.dataflow.service.DataflowService;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ExternalExportDesignCompletedEventTest {

  @InjectMocks
  private ExternalExportDesignCompletedEvent externalExportDesignCompletedEvent;

  @Mock
  private DataflowService dataflowService;

  @Mock
  private DataSetMetabaseControllerZuul dataSetMetabaseControllerZuul;

  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.EXTERNAL_EXPORT_DESIGN_COMPLETED_EVENT,
        externalExportDesignCompletedEvent.getEventType());
  }

  @Test
  public void getMapTest() throws EEAException {
    DataSetMetabaseVO dataSetMetabaseVO = Mockito.mock(DataSetMetabaseVO.class);
    DataFlowVO dataFlowVO = Mockito.mock(DataFlowVO.class);
    Mockito.when(dataSetMetabaseControllerZuul.findDatasetMetabaseById(Mockito.anyLong()))
        .thenReturn(dataSetMetabaseVO);
    Mockito.when(dataSetMetabaseVO.getDataSetName()).thenReturn("datasetName");
    Mockito.when(dataflowService.getMetabaseById(Mockito.anyLong())).thenReturn(dataFlowVO);
    Mockito.when(dataFlowVO.getName()).thenReturn("dataflowName");
    Assert.assertEquals(6,
        externalExportDesignCompletedEvent.getMap(NotificationVO.builder().user("user")
            .datasetId(1L).dataflowId(1L).fileName("fileName").providerId(1L).build()).size());
  }
}
