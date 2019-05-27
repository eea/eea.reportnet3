package org.eea.kafka.serializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.eea.kafka.domain.EEAEventVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class EEAEventDeserializer.
 */
public class EEAEventDeserializer implements Deserializer<EEAEventVO> {

  /** The Constant LOG. */
  private static final Logger LOG = LoggerFactory.getLogger(EEAEventDeserializer.class);

  /**
   * Configure.
   *
   * @param map the map
   * @param b the b
   */
  @Override
  public void configure(final Map<String, ?> map, final boolean b) {

  }

  /**
   * Deserialize.
   *
   * @param topic the topic
   * @param bytes the bytes
   * @return the EEA event VO
   */
  @Override
  public EEAEventVO deserialize(final String topic, final byte[] bytes) {
    Object event = null;
    try {
      final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
      final ObjectInputStream inputStream = new ObjectInputStream(byteArrayInputStream);
      event = inputStream.readObject();

      inputStream.close();

    } catch (final IOException | ClassNotFoundException | ClassCastException e) {
      if (event != null) {
        LOG.info(event.toString());
      }
    }
    return (EEAEventVO) event;
  }

  /**
   * Close.
   */
  @Override
  public void close() {

  }
}
