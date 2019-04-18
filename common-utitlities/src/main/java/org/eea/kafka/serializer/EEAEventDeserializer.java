package org.eea.kafka.serializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.eea.kafka.domain.EEAEventVO;

public class EEAEventDeserializer implements Deserializer<EEAEventVO> {

  @Override
  public void configure(final Map<String, ?> map, final boolean b) {

  }

  @Override
  public EEAEventVO deserialize(final String topic, final byte[] bytes) {
    Object event = null;
    try {
      final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
      final ObjectInputStream inputStream = new ObjectInputStream(byteArrayInputStream);
      event = inputStream.readObject();

      inputStream.close();

    } catch (final IOException | ClassNotFoundException | ClassCastException e) {
      e.printStackTrace();
      System.out.println(event.toString());
    }
    return (EEAEventVO) event;
  }

  @Override
  public void close() {

  }
}
