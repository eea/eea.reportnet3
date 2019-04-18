package org.eea.kafka.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
import org.apache.kafka.common.serialization.Serializer;

/**
 * The type Eea event serializer.
 */
public class EEAEventSerializer implements Serializer {

  @Override
  public void configure(Map map, boolean b) {

  }

  @Override
  public byte[] serialize(String topic, Object o) {
    try {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
      outputStream.writeObject(o);
      outputStream.close();

      return byteArrayOutputStream.toByteArray();
    } catch (IOException e) {
      return new byte[0];
    }
  }

  @Override
  public void close() {

  }
}
