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

public class FieldValueGenerator implements IdentifierGenerator {

  @Override
  public Serializable generate(SharedSessionContractImplementor session, Object object)
      throws HibernateException {

    FieldValue field = new FieldValue();
    field = (FieldValue) object;
    String prefix = null;
    // Set the provider code to create Hash
    if (null == field.getRecord().getDataProviderCode())
      prefix = "AUX" + field.getRecord().getTableValue().getDatasetId().getId().toString();
    else {
      prefix = field.getRecord().getDataProviderCode();
    }
    // Connection must not close because transaction not finished yet.
    Connection connection = session.connection(); // NOPMD
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery("SELECT nextval('field_sequence')");

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
      try {
        if (null != rs) {
          rs.close();
        }
        if (statement != null) {
          statement.close();
        }
        if (null != connection) {
          connection.close();
        }
      } catch (SQLException i) {
        i.printStackTrace();
      }
    }
    return null;
  }
}
