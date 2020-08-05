package org.eea.dataset.persistence.data.sequence;

import java.io.Serializable;
import java.util.UUID;
import org.apache.commons.codec.digest.DigestUtils;
import org.eea.dataset.persistence.data.domain.AttachmentValue;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AttachmentValueIdGenerator implements IdentifierGenerator {

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

    AttachmentValue attachment = (AttachmentValue) object;
    String prefix = null;
    String datasetId =
        attachment.getFieldValue().getRecord().getTableValue().getDatasetId().getId().toString();
    // Set the provider code to create Hash
    if (null == attachment.getFieldValue().getRecord().getDataProviderCode()) {
      Double aux = Math.random();
      prefix = "ATTACHMENT" + aux.toString() + "DS";
    } else {
      prefix = "ATTACHMENT" + attachment.getFieldValue().getRecord().getDataProviderCode();
    }

    String idcompose = datasetId + prefix + UUID.randomUUID();
    return DigestUtils.md5Hex(idcompose).toUpperCase();

  }
}
