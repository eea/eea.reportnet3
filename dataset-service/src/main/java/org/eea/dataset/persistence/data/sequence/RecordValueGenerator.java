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

public class RecordValueGenerator implements IdentifierGenerator {

  @Override
  public Serializable generate(SharedSessionContractImplementor session, Object object)
      throws HibernateException {

    RecordValue record = new RecordValue();
    record = (RecordValue) object;
    String prefix = null;
    // Set the provider code to create Hash
    if (null == record.getDataProviderCode())
      prefix = "AUX" + record.getTableValue().getDatasetId().getId().toString();
    else {
      prefix = record.getDataProviderCode();
    }
    Connection connection = session.connection();

    try {
      Statement statement = connection.createStatement();
      ResultSet rs = statement.executeQuery("SELECT nextval('record_sequence')");

      if (rs.next()) {
        int id = rs.getInt(1);
        String idcompose = prefix + new Integer(id).toString();
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


