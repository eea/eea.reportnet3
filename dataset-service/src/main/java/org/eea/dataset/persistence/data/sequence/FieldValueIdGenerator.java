package org.eea.dataset.persistence.data.sequence;

import java.io.Serializable;
import java.util.UUID;
import org.apache.commons.codec.digest.DigestUtils;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * The Class FieldValueGenerator.
 */
@Component
public class FieldValueIdGenerator implements IdentifierGenerator {

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

    FieldValue field = (FieldValue) object;
    String prefix = null;
    String datasetId = "";
    if (field != null && field.getRecord() != null && field.getRecord().getTableValue() != null
        && field.getRecord().getTableValue().getDatasetId() != null) {
      datasetId = field.getRecord().getTableValue().getDatasetId().getId().toString();
      // Set the provider code to create Hash
      if (null == field.getRecord().getDataProviderCode()) {
        Double aux = Math.random();
        prefix = "FIELD" + aux.toString() + "DS";
      } else {
        prefix = "FIELD" + field.getRecord().getDataProviderCode();
      }
    } else {
      LOG_ERROR.error("Error generating field serial id number. Field {}", field);
    }
    String idcompose = datasetId + prefix + UUID.randomUUID();
    return DigestUtils.md5Hex(idcompose).toUpperCase();

  }
}
