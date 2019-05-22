package org.eea.dataset.configuration;

import org.eea.interfaces.controller.recordstore.RecordStoreController.RecordStoreControllerZull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DatasetConfigurationTest {

  @InjectMocks
  DatasetConfiguration datasetConfiguration;

  @Mock
  RecordStoreControllerZull recordStoreControllerZull;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testDataSource() throws Exception {
    // TODO
    throw new RuntimeException("not yet implemented");
  }



  @Test
  public void testTargetDataSources() throws Exception {
    // TODO
    throw new RuntimeException("not yet implemented");
  }

  @Test
  public void testDataSetsEntityManagerFactory() throws Exception {
    // TODO
    throw new RuntimeException("not yet implemented");
  }

  @Test
  public void testAdditionalProperties() throws Exception {
    // TODO
    throw new RuntimeException("not yet implemented");
  }

  @Test
  public void testDataSetsTransactionManager() throws Exception {
    // TODO
    throw new RuntimeException("not yet implemented");
  }

}
