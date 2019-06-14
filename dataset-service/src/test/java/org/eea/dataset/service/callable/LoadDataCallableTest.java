package org.eea.dataset.service.callable;

import static org.mockito.Mockito.doNothing;
import java.io.InputStream;
import org.eea.dataset.service.DatasetService;
import org.eea.kafka.io.KafkaSender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class LoadDataCallableTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class LoadDataCallableTest {

  @InjectMocks
  LoadDataCallable callable;

  @Mock
  InputStream is;

  @Mock
  DatasetService datasetService;

  @Mock
  KafkaSender kafka;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test load data callable.
   */
  @Test
  public void testLoadDataCallable() {
    LoadDataCallable call = new LoadDataCallable(kafka, datasetService, 1L, "", is, "");
  }

  /**
   * Test call.
   * 
   * @throws Exception
   */
  @Test
  public void testCall() throws Exception {
    doNothing().when(datasetService).processFile(Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any());
    callable.call();
  }

}
