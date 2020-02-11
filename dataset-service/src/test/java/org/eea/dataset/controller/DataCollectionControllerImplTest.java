package org.eea.dataset.controller;

import org.eea.dataset.service.DataCollectionService;
import org.eea.dataset.service.DatasetMetabaseService;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.dataset.service.DesignDatasetService;
import org.eea.interfaces.controller.dataflow.DataFlowController.DataFlowControllerZuul;
import org.eea.interfaces.controller.dataflow.RepresentativeController.RepresentativeControllerZuul;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * The Class DataCollectionControllerImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataCollectionControllerImplTest {

  @InjectMocks
  private DataCollectionControllerImpl dataCollectionControllerImpl;

  @Mock
  private RepresentativeControllerZuul representativeControllerZuul;

  @Mock
  private DataFlowControllerZuul dataflowControllerZuul;

  @Mock
  private DataCollectionService dataCollectionService;

  @Mock
  private DatasetMetabaseService datasetMetabaseService;

  @Mock
  private DesignDatasetService designDatasetService;

  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  @Mock
  private DatasetSchemaService schemaService;

  @Mock
  private UserManagementControllerZull userManagementControllerZuul;

  /** The security context. */
  SecurityContext securityContext;

  /** The authentication. */
  Authentication authentication;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {

    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void findDataCollectionIdByDataflowIdTest() {
    Mockito.when(dataCollectionService.getDataCollectionIdByDataflowId(Mockito.any()))
        .thenReturn(null);
    Assert.assertNull(dataCollectionControllerImpl.findDataCollectionIdByDataflowId(1L));
  }
}
