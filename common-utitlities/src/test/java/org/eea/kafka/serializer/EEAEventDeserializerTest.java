package org.eea.kafka.serializer;

import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EEAEventDeserializerTest {


  @InjectMocks
  private EEAEventDeserializer eEAEventDeserializer;


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
    String topic = "topico";
    byte[] bytes = new byte[5];
    eEAEventDeserializer.deserialize(topic, bytes);
  }

  @Test
  public void testFull() {
    String topic = "topico";
    byte[] buffer = new byte[] {-84, -19, 0, 5};
    eEAEventDeserializer.deserialize(topic, buffer);
  }
}
