package org.eea.dataset.io.kafka.commands;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.eea.dataset.service.helper.UpdateRecordHelper;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.enums.DataType;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.lock.service.LockService;
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
 * The Class SaveStatisticsCommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class PropagateNewFieldCommandTest {


  /** The save statistics command. */
  @InjectMocks
  private PropagateNewFieldCommand propagateCommand;


  /** The update record helper. */
  @Mock
  private UpdateRecordHelper updateRecordHelper;

  /** The eea event VO. */
  private EEAEventVO eeaEventVO;

  /** The data. */
  private Map<String, Object> data;

  /** The lock service. */
  @Mock
  private LockService lockService;

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
   * Test execute.
   *
   * @throws EEAException the EEA exception
   */
  @Test
  public void testExecute() throws EEAException {

    eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.COMMAND_NEW_DESIGN_FIELD_PROPAGATION);
    data = new HashMap<>();
    data.put("dataset_id", 1L);
    data.put("sizeRecords", 1);
    data.put("idTableSchema", "5cf0e9b3b793310e9ceca190");
    data.put("idFieldSchema", "5cf0e9b3b793310e9ceca190");
    data.put("typeField", DataType.TEXT);
    eeaEventVO.setData(data);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn("user");
    doNothing().when(updateRecordHelper).propagateNewFieldDesign(Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    propagateCommand.execute(eeaEventVO);
    Mockito.verify(updateRecordHelper, times(1)).propagateNewFieldDesign(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

  }


  /**
   * Gets the event type test.
   *
   * @return the event type test
   */
  @Test
  public void getEventTypeTest() {
    assertEquals(EventType.COMMAND_NEW_DESIGN_FIELD_PROPAGATION, propagateCommand.getEventType());
  }

}
