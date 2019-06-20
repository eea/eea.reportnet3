package org.eea.validation.util;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import org.eea.exception.EEAException;
import org.eea.kafka.utils.KafkaSenderUtils;
import org.eea.validation.service.ValidationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class ValidationHelperTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationHelperTest {

  /** The validation helper. */
  @InjectMocks
  private ValidationHelper validationHelper;

  /** The kafka sender utils. */
  @Mock
  private KafkaSenderUtils kafkaSenderUtils;

  /** The validation service. */
  @Mock
  private ValidationService validationService;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testKafkaHelper() throws EEAException {
    doNothing().when(validationService).validateDataSetData(Mockito.any());
    doNothing().when(kafkaSenderUtils).releaseDatasetKafkaEvent(Mockito.any(), Mockito.any());
    validationHelper.executeValidation(1L);
    Mockito.verify(kafkaSenderUtils, times(1)).releaseDatasetKafkaEvent(Mockito.any(),
        Mockito.any());
  }

}
