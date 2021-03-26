package org.eea.dataset.service.helper;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;
import org.eea.interfaces.vo.recordstore.ConnectionDataVO;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;

/**
 * The Class PostgresBulkImporter.
 */
public class PostgresBulkImporter implements Closeable {

  /** The url. */
  private final String url;

  /** The user. */
  private final String user;

  /** The password. */
  private final String password;

  /** The schema. */
  private final String schema;

  /** The table name. */
  private final String tableName;

  /** The temporary file. */
  private final File temporaryFile;

  /** The output stream. */
  private OutputStream outputStream;

  /**
   * Instantiates a new postgres bulk importer.
   *
   * @param connectionDataVO the connection data VO
   * @param schema the schema
   * @param tableName the table name
   * @param path the path
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public PostgresBulkImporter(ConnectionDataVO connectionDataVO, String schema, String tableName,
      String path) throws IOException {
    url = connectionDataVO.getConnectionString();
    user = connectionDataVO.getUser();
    password = connectionDataVO.getPassword();
    this.schema = schema;
    this.tableName = tableName;
    temporaryFile = new File(path, UUID.randomUUID().toString() + ".bin");
    outputStream = new FileOutputStream(temporaryFile);
    writeHeaders();
  }

  /**
   * Adds the tuple.
   *
   * @param values the values
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void addTuple(Object[] values) throws IOException {
    // Number of columns. 2 Bytes
    outputStream.write(Shorts.toByteArray((short) values.length));

    // Write each field
    for (Object value : values) {
      if (value instanceof String) {
        writeString(outputStream, (String) value);
      } else if (value instanceof Long) {
        writeLong(outputStream, (Long) value);
      } else {
        writeNull(outputStream);
      }
    }
  }

  /**
   * Copy.
   *
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public void copy() throws SQLException, IOException {
    // Close the OutputStream to be able to open an InputStream to the same file
    outputStream.close();

    // Execute the COPY sentence to import the binary file into PostgreSQL
    try (FileInputStream inputStream = new FileInputStream(temporaryFile);
        Connection connection = DriverManager.getConnection(url, user, password)) {
      String query = "copy " + schema + "." + tableName + " from stdin with binary";
      CopyManager copyManager = new CopyManager((BaseConnection) connection);
      copyManager.copyIn(query, inputStream);
    }
  }

  /**
   * Close.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Override
  public void close() throws IOException {
    try {
      if (null != temporaryFile) {
        Files.deleteIfExists(temporaryFile.toPath());
      }
    } finally {
      if (null != outputStream) {
        outputStream.close();
      }
    }
  }

  /**
   * Write headers.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void writeHeaders() throws IOException {
    // Signature. 11 Bytes. PGCOPY\n\377\r\n\0
    outputStream.write(new byte[] {80, 71, 67, 79, 80, 89, 10, -1, 13, 10, 0});
    // Flags. 4 Bytes.
    outputStream.write(new byte[] {0, 0, 0, 0});
    // Header extension. 4 Bytes
    outputStream.write(new byte[] {0, 0, 0, 0});
  }

  /**
   * Write string.
   *
   * @param outputStream the output stream
   * @param value the value
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void writeString(OutputStream outputStream, String value) throws IOException {
    byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
    // Length in bytes of the value. 4 bytes
    outputStream.write(Ints.toByteArray(bytes.length));
    // Value. N Bytes.
    outputStream.write(bytes);
  }

  /**
   * Write long.
   *
   * @param outputStream the output stream
   * @param value the value
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void writeLong(OutputStream outputStream, Long value) throws IOException {
    // Length in bytes of the value. 4 bytes
    outputStream.write(new byte[] {0, 0, 0, 8});
    // Byte representation of the Long. 8 Bytes.
    outputStream.write(Longs.toByteArray(value));
  }

  /**
   * Write null.
   *
   * @param outputStream the output stream
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void writeNull(OutputStream outputStream) throws IOException {
    // Representation of null. 4 Bytes.
    outputStream.write(new byte[] {-1, -1, -1, -1});
  }
}
