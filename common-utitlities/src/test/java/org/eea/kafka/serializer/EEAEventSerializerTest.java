package org.eea.kafka.serializer;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EEAEventSerializerTest {



  @InjectMocks
  private EEAEventSerializer eEAEventSerializer;


  /**
   * Inits the mocks.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testError() {
    eEAEventSerializer.serialize("", new Object());
  }

  /**
   * Test full.
   */
  @Test
  public void testFull() {
    eEAEventSerializer.serialize("topicTestPass", "ObjectToPass");
  }
}
