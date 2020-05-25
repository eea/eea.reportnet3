package org.eea.validation.kafka.command;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.eea.exception.EEAException;
import org.eea.kafka.domain.EEAEventVO;
import org.eea.kafka.domain.EventType;
import org.eea.validation.util.ValidationHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieBase;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

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
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testExecute() {
    Map<String, Object> data = new HashMap<>();
    data.put("uuid", "uuid");
    data.put("dataset_id", "1");
    EEAEventVO eeaEventVO = new EEAEventVO();
    eeaEventVO.setEventType(EventType.COMMAND_VALIDATED_DATASET_COMPLETED);
    eeaEventVO.setData(data);
    executeValidationProcessCommand.execute(eeaEventVO);
    Mockito.verify(validationHelper,
        Mockito.times(1)).executeValidation(Mockito.eq(1l), Mockito.anyString());
  }

}
