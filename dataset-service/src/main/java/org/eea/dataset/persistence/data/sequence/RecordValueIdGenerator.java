package org.eea.dataset.persistence.data.sequence;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.codec.digest.DigestUtils;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class RecordValueGenerator.
 */
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
   * @return the serializable
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
    // Connection must not close because transaction not finished yet.
    Connection connection = session.connection();// NOPMD

    try (Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT nextval('record_sequence')")) {

      if (rs.next()) {
        int id = rs.getInt(1);
        String idcompose = datasetId + prefix + Integer.valueOf(id);
        return DigestUtils.md5Hex(idcompose).toUpperCase();

      }
    } catch (SQLException e) {
      LOG_ERROR.error("Faliled to generate Field ID");
    }
    return null;
  }
}
