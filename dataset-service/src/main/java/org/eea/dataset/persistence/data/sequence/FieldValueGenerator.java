package org.eea.dataset.persistence.data.sequence;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.codec.digest.DigestUtils;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

/**
 * The type Field value generator.
 */
public class FieldValueGenerator implements IdentifierGenerator {

  @Override
  public Serializable generate(SharedSessionContractImplementor session, Object object)
      throws HibernateException {

    FieldValue field = (FieldValue) object;
    String prefix = null;
    // Set the provider code to create Hash
    if (null == field.getRecord().getDataProviderCode()) {
      prefix = "AUX" + field.getRecord().getTableValue().getDatasetId().getId().toString();
    } else {
      prefix = field.getRecord().getDataProviderCode();
    }
    // Connection must not close because transaction not finished yet.
    Connection connection = session.connection(); // NOPMD

    try {
      Statement statement = connection // NOPMD
          .createStatement(); // NOSONAR statement must not be closed in order to allow the operation to go on
      ResultSet rs = statement.executeQuery( // NOPMD
          "SELECT nextval('field_sequence')");// NOPMD resultset must not be closed in order to allow the operation to go on

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

      rs.close();
      statement.close();
      connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }
}
