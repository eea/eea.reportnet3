package org.eea.dataset.persistence.data.sequence;

import java.io.Serializable;
import java.util.UUID;
import org.apache.commons.codec.digest.DigestUtils;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.stereotype.Component;

/**
 * The Class RecordValueGenerator.
 */
@Component
public class RecordValueIdGenerator implements IdentifierGenerator {


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
    String prefix = null;
    String datasetId = record.getTableValue().getDatasetId().getId().toString();
    // Set the provider code to create Hash
    if (null == record.getDataProviderCode()) {
      Double aux = Math.random();
      prefix = "RECORD" + aux.toString() + "DS";
    } else {
      prefix = record.getDataProviderCode();
    }
    String idcompose = datasetId + prefix + UUID.randomUUID();
    return DigestUtils.md5Hex(idcompose).toUpperCase();

  }
}
