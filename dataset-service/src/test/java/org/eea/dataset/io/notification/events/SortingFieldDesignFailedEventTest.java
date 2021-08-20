package org.eea.dataset.io.notification.events;

import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetService;
import org.eea.exception.EEAException;
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

/**
 * The Class SortingFieldDesignFailedEventTest.
 */
public class SortingFieldDesignFailedEventTest {


  /** The sorting field failed event. */
  @InjectMocks
  private SortingFieldDesignFailedEvent sortingFieldDesignFailedEvent;

  /** The dataset metabase service. */
  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  /** The dataset metabase service. */
  @Mock
  private DatasetService datasetService;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Test get event type.
   */
  @Test
  public void testGetEventType() {
    Assert.assertEquals(EventType.SORT_FIELD_DESIGN_FAILED_EVENT,
        sortingFieldDesignFailedEvent.getEventType());
  }

  /**
   * Test get map.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testGetMap() throws EEAException {
    DataSetMetabaseVO dataSetMetabaseVO = new DataSetMetabaseVO();
    dataSetMetabaseVO.setDataSetName("datasetName");
    Mockito.when(datasetMetabaseService.findDatasetMetabase(Mockito.any()))
        .thenReturn(dataSetMetabaseVO);
    Mockito.when(datasetService.getDataFlowIdById(Mockito.any())).thenReturn(1L);

    Assert.assertEquals("datasetName",
        sortingFieldDesignFailedEvent.getMap(NotificationVO.builder().user("user").datasetId(1L)
            .datasetName("datasetName").tableSchemaId("1234").tableSchemaName("tableSchemaName")
            .fieldSchemaId("1234").fieldSchemaName("fieldSchemaName").error("error").build())
            .get("datasetName"));
  }

}
