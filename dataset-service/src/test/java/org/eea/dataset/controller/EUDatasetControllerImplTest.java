package org.eea.dataset.controller;

import org.eea.dataset.service.EUDatasetService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;


/**
 * The Class EUDatasetControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class EUDatasetControllerImplTest {

  @InjectMocks
  private EUDatasetControllerImpl euDatasetControllerImpl;


  @Mock
  private EUDatasetService euDatasetService;



  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void findEUDatasetByDataflowIdTest() {
    Mockito.when(euDatasetService.getEUDatasetByDataflowId(Mockito.any())).thenReturn(null);
    Assert.assertNull(euDatasetControllerImpl.findEUDatasetByDataflowId(1L));
  }


}
