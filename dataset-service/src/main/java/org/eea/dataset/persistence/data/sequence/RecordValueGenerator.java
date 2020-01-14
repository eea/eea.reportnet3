package org.eea.dataset.persistence.data.sequence;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.codec.digest.DigestUtils;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

/**
 * The type Record value generator.
 */
public class RecordValueGenerator implements IdentifierGenerator {

  @Override
  public Serializable generate(SharedSessionContractImplementor session, Object object)
      throws HibernateException {

    RecordValue record = (RecordValue) object;
    String prefix = null;
    // Set the provider code to create Hash
    if (null == record.getDataProviderCode()) {
      prefix = "AUX" + record.getTableValue().getDatasetId().getId().toString();
    } else {
      prefix = record.getDataProviderCode();
    }
    // Connection must not close because transaction not finished yet.
    Connection connection = session.connection();// NOPMD
    try {
      Statement statement = connection // NOPMD 
          .createStatement();// NOSONAR statement must not be closed in order to allow the operation to go on
      ResultSet rs = statement.executeQuery( // NOPMD
          "SELECT nextval('record_sequence')");// NOPMD resultset must not be closed in order to allow the operation to go on

      if (rs.next()) {
        int id = rs.getInt(1);
        String idcompose = prefix + Integer.valueOf(id);
        String md5Hex = DigestUtils.md5Hex(idcompose).toUpperCase();
        BigInteger bi = new BigInteger(md5Hex, 16);
        Long hexId = bi.longValue();
        String textId = hexId.toString();
        Long hashId = Long.parseLong(textId.substring(0, 14));
        return hashId;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }
}
