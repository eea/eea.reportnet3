package org.eea.validation.util;

import static org.mockito.Mockito.doNothing;
import org.eea.exception.EEAException;
import org.eea.kafka.io.KafkaSender;
import org.eea.validation.service.ValidationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ValidationHelperTest {


  @InjectMocks
  private ValidationHelper validationHelper;
  @Mock
  private ValidationService validationService;
  @Mock
  private KafkaSender kafkaSender;


  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testKafkaHelper() throws EEAException {
    doNothing().when(kafkaSender).sendMessage(Mockito.any());
    ValidationHelper.executeValidation(kafkaSender, validationService, 1L);
  }

}
