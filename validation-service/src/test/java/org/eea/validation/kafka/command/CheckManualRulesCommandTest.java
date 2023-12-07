package org.eea.validation.kafka.command;

import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.validation.service.impl.RulesServiceImpl;
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

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * The Class CheckManualRulesCommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class CheckManualRulesCommandTest {
  /**
   * The Check manual rules command.
   */
  @InjectMocks
  private CheckManualRulesCommand CheckManualRulesCommand;

  @Mock
  private RulesServiceImpl rulesService;

  /**
   * The security context.
   */
  private SecurityContext securityContext;

  /**
   * The authentication.
   */
  private Authentication authentication;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {

    authentication = Mockito.mock(Authentication.class);
    securityContext = Mockito.mock(SecurityContext.class);
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Execute test.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void executeTest() throws EEAException {
    Map<String, Object> data = new HashMap<>();
    data.put("dataset_id", "1");
    data.put("user", "user");
    data.put("checkNoSQL", Boolean.TRUE);
    EEAEventVO eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.VALIDATE_MANUAL_QC_COMMAND);
    eeaEventVO.setData(data);
    Mockito.doNothing().when(rulesService).validateAllRules(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString());

    CheckManualRulesCommand.execute(eeaEventVO);
    Mockito.verify(rulesService, Mockito.times(1))
        .validateAllRules(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyString());
  }

  /**
   * Gets the event type test.
   *
   * @return the event type test
   */
  @Test
  public void getEventTypeTest() {
    assertEquals(EventType.VALIDATE_MANUAL_QC_COMMAND, CheckManualRulesCommand.getEventType());
  }

}
