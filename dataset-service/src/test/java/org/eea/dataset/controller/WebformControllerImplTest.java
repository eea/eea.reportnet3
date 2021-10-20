package org.eea.dataset.controller;

import static org.junit.Assert.assertNotNull;
import org.eea.dataset.service.WebformService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class WebformControllerImplTest {


  @InjectMocks
  private WebformControllerImpl webFormControllerImpl;

  @Mock
  private WebformService webformservice;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Tests the method to retrieve the list of webforms
   *
   * @return the list of webforms that will be checked
   */
  @Test
  public void getListWebformsTest() {
    assertNotNull(webFormControllerImpl.getListWebforms());
  }

}
