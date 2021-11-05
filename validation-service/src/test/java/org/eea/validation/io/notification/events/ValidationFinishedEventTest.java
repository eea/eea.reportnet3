package org.eea.validation.io.notification.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetController.DataSetControllerZuul;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController.DataSetMetabaseControllerZuul;
import org.eea.interfaces.vo.dataflow.DataFlowVO;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.DesignDatasetVO;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.eea.kafka.domain.EventType;
import org.eea.kafka.domain.NotificationVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ValidationFinishedEventTest {

  @InjectMocks
  private ValidationFinishedEvent validationFinishedEvent;

  @Mock
  private DataSetControllerZuul dataSetControllerZuul;

  @Mock
  private DataSetMetabaseControllerZuul datasetMetabaseController;

  @Mock
  private DataSetMetabaseVO datasetVO;

  @Mock
  private DataFlowVO dataflowVO;

  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.VALIDATION_FINISHED_EVENT,
        validationFinishedEvent.getEventType());
  }


  // @Test
  public void getMapFromMinimumDataTest() throws EEAException {

    Mockito.when(datasetMetabaseController.findDatasetMetabaseById(Mockito.any()))
        .thenReturn(datasetVO);
    Mockito.when(datasetVO.getDataSetName()).thenReturn("datasetName");
    Mockito.when(datasetVO.getDatasetSchema()).thenReturn("602154a699827a6a72828ef2");
    Mockito.when(datasetVO.getDatasetTypeEnum()).thenReturn(DatasetTypeEnum.REPORTING);
    Mockito.when(datasetVO.getDataflowId()).thenReturn(2L);
    Mockito.when(dataflowVO.getName()).thenReturn("dataflowName");

    List<DesignDatasetVO> desingDatasetList = new ArrayList();
    DesignDatasetVO designDatasetVO = new DesignDatasetVO();
    designDatasetVO.setDatasetSchema("602154a699827a6a72828ef2");
    designDatasetVO.setDataSetName("datasetName");
    desingDatasetList.add(designDatasetVO);
    Mockito.when(datasetMetabaseController.findDesignDataSetIdByDataflowId(Mockito.anyLong()))
        .thenReturn(desingDatasetList);


    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Mockito.when(dataflowControllerZuul.getMetabaseById(Mockito.anyLong())).thenReturn(dataflowVO);
    Map<String, Object> result =
        validationFinishedEvent.getMap(NotificationVO.builder().user("user").datasetId(1L).build());
    Assert.assertEquals(7, result.size());
    Assert.assertEquals("user", result.get("user"));
    Assert.assertEquals(1L, result.get("datasetId"));
    Assert.assertEquals(2L, result.get("dataflowId"));
    Assert.assertEquals("datasetName", result.get("datasetName"));
    Assert.assertEquals("dataflowName", result.get("dataflowName"));
    Assert.assertEquals(DatasetTypeEnum.REPORTING, result.get("type"));
  }
}

