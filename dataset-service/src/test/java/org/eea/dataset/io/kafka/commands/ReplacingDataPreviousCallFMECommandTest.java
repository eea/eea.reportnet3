package org.eea.dataset.io.kafka.commands;

import static org.mockito.Mockito.times;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.eea.dataset.service.helper.FileTreatmentHelper;
import org.eea.interfaces.controller.dataflow.IntegrationController.IntegrationControllerZuul;
import org.eea.interfaces.vo.integration.IntegrationVO;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.lock.service.LockService;
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
 * The Class ReplacingDataPreviousCallFMECommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReplacingDataPreviousCallFMECommandTest {


  @InjectMocks
  private ReplacingDataPreviousFMECallCommand replacingDataPreviousFMECallCommand;


  @Mock
  private FileTreatmentHelper fileTreatmentHelper;

  @Mock
  private IntegrationControllerZuul integrationController;

  @Mock
  private LockService lockService;

  @Mock
  File mockFile;


  /** The eea event VO. */
  private EEAEventVO eeaEventVO;

  /** The data. */
  private Map<String, Object> data;

  /** The security context. */
  private SecurityContext securityContext;

  /** The authentication. */
  private Authentication authentication;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.CONTINUE_FME_PROCESS_EVENT);
    data = new HashMap<>();
    data.put("datasetId", 1L);
    data.put("integrationId", 1L);
    data.put("fileName", "test.csv");
    eeaEventVO.setData(data);

    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void getEventTypeTest() {
    Assert.assertEquals(EventType.CONTINUE_FME_PROCESS_EVENT,
        replacingDataPreviousFMECallCommand.getEventType());
  }


  @Test
  public void testExecute() {
    IntegrationVO integrationVO = new IntegrationVO();
    integrationVO.setExternalParameters(new HashMap<>());
    Mockito.when(integrationController.findIntegrationById(Mockito.anyLong()))
        .thenReturn(integrationVO);
    replacingDataPreviousFMECallCommand.execute(eeaEventVO);
    Mockito.verify(integrationController, times(1)).findIntegrationById(Mockito.anyLong());
  }

}
