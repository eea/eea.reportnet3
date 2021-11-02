package org.eea.dataset.persistence.data.sequence;

import java.io.Serializable;
import java.util.UUID;
import org.apache.commons.codec.digest.DigestUtils;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The Class RecordValueGenerator.
 */
@Component
public class RecordValueIdGenerator implements IdentifierGenerator {

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Generate.
   *
   * @param session the session
   * @param object the object
   *
   * @return the serializable
   *
   * @throws HibernateException the hibernate exception
   */
  @Override
  public Serializable generate(SharedSessionContractImplementor session, Object object)
      throws HibernateException {

    RecordValue record = (RecordValue) object;
    String prefix = Double.toString(Math.random());
    String datasetId = "";
    if (record != null && record.getTableValue() != null
        && record.getTableValue().getDatasetId() != null
        && record.getTableValue().getDatasetId().getId() != null) {
      datasetId = record.getTableValue().getDatasetId().getId().toString();
      // Set the provider code to create Hash
      if (null == record.getDataProviderCode()) {
        Double aux = Math.random();
        prefix = "RECORD" + aux.toString() + "DS";
      } else {
        prefix = record.getDataProviderCode();
      }
    } else {
      LOG_ERROR.error("Error generating record serial id number. Record {}", record);
    }
    String idcompose = datasetId + prefix + UUID.randomUUID();
    return DigestUtils.md5Hex(idcompose).toUpperCase();

  }
}
