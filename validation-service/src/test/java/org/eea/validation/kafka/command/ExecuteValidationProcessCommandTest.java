package org.eea.validation.kafka.command;

import java.util.HashMap;
import java.util.Map;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.validation.util.ValidationHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class ExecuteValidationCommandTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecuteValidationProcessCommandTest {

  @InjectMocks
  private ExecuteValidationProcessCommand executeValidationProcessCommand;

  @Mock
  private ValidationHelper validationHelper;

  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testExecute() throws EEAException {
    Map<String, Object> data = new HashMap<>();
    data.put("uuid", "uuid");
    data.put("dataset_id", "1");
    data.put("user", "user1");
    EEAEventVO eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.COMMAND_VALIDATED_DATASET_COMPLETED);

    eeaEventVO.setData(data);
    executeValidationProcessCommand.execute(eeaEventVO);
    Mockito.verify(validationHelper, Mockito.times(1)).executeValidation(Mockito.eq(1l),
        Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean());
  }

}
