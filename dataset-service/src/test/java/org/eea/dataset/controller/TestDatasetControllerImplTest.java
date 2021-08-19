package org.eea.dataset.controller;

import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.service.TestDatasetService;
import org.eea.interfaces.vo.dataset.TestDatasetVO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The Class TestDatasetControllerImplTest.
 */
public class TestDatasetControllerImplTest {

  /** The test dataset service. */
  @Mock
  private TestDatasetService testDatasetService;

  /** The test dataset controller impl. */
  @InjectMocks
  private TestDatasetControllerImpl testDatasetControllerImpl;

  /** The list test datasets. */
  private List<TestDatasetVO> listTestDatasets;

  /** The security context. */
  SecurityContext securityContext;

  /** The authentication. */
  Authentication authentication;

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    listTestDatasets = new ArrayList<>();
    TestDatasetVO testDataset = new TestDatasetVO();
    testDataset.setId(1L);
    listTestDatasets.add(testDataset);

    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);

    MockitoAnnotations.openMocks(this);
  }

  /**
   * Test find test dataset by dataflow id.
   *
   * @throws Exception the exception
   */
  @Test
  public void testFindTestDatasetByDataflowId() throws Exception {
    Mockito.when(testDatasetService.getTestDatasetByDataflowId(Mockito.any()))
        .thenReturn(listTestDatasets);
    Assert.assertEquals(listTestDatasets,
        testDatasetControllerImpl.findTestDatasetByDataflowId(1L));
  }

}
