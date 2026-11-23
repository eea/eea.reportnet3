package org.eea.datalake.service.impl;

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.FileUtils;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.io.OutputFile;
import org.apache.parquet.io.PositionOutputStream;
import org.eea.datalake.service.S3Helper;
import org.eea.datalake.service.SpatialDataHandling;
import org.eea.interfaces.vo.dataset.enums.DatasetTypeEnum;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * Tests for the CSV export of {@link S3ConvertServiceImpl}. Every exported CSV must start with a
 * UTF-8 BOM and always contain the header row, also for empty tables (ATA2-3370 / EEA 310146).
 */
public class S3ConvertServiceImplTest {

  private static final byte[] BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
  private static final String TABLE = "SiteCode";
  private static final String KEY =
      "df-0000071/dp-0000001/ds-0000290/current/" + TABLE + "/123_" + TABLE + "_x/0_0_0.parquet";
  private static final String OTHER_TABLE_KEY = KEY.replace("/" + TABLE + "/", "/OtherTable/");

  @Mock
  private S3Helper s3Helper;

  @Mock
  private SpatialDataHandling spatialDataHandling;

  private S3ConvertServiceImpl service;
  private Path work;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
    service = new S3ConvertServiceImpl(spatialDataHandling, s3Helper);
    work = Files.createTempDirectory("s3ConvertServiceImplTest");
    Field exportDLPath = S3ConvertServiceImpl.class.getDeclaredField("exportDLPath");
    exportDLPath.setAccessible(true);
    exportDLPath.set(service, work.toString() + File.separator);
  }

  @After
  public void tearDown() throws IOException {
    FileUtils.deleteDirectory(work.toFile());
  }

  @Test
  public void createCSVFileEmptyParquetWritesBomAndHeaders() throws Exception {
    serve(parquet("empty.parquet", 0));
    assertArrayEquals(bomAnd("site_code\n"), export(KEY));
  }

  @Test
  public void createCSVFileParquetOfOtherTableWritesBomAndHeaders() throws Exception {
    serve(parquet("one.parquet", 1));
    assertArrayEquals(bomAnd("site_code\n"), export(OTHER_TABLE_KEY));
  }

  @Test
  public void createCSVFileWithRecordsWritesBomHeaderAndRows() throws Exception {
    serve(parquet("one.parquet", 1));
    assertArrayEquals(bomAnd("site_code\nES0000\n"), export(KEY));
  }

  @Test
  public void createEmptyCSVFileWritesBomAndHeaders() throws Exception {
    File csv = service.createEmptyCSVFile(TABLE, 290L, headers(), false, null, null);
    assertArrayEquals(bomAnd("site_code\n"), Files.readAllBytes(csv.toPath()));
  }

  @Test
  public void createCSVFileWithProvidersEmptyParquetWritesBomAndHeaders() throws Exception {
    serve(parquet("empty.parquet", 0));
    assertArrayEquals(bomAnd("site_code\n"), exportWithProviders(KEY));
  }

  @Test
  public void createCSVFileWithProvidersWithRecordsWritesBomHeaderAndRows() throws Exception {
    serve(parquet("one.parquet", 1));
    assertArrayEquals(bomAnd("site_code\nES0000\n"), exportWithProviders(KEY));
  }

  @Test
  public void createEmptyCSVFileWithProvidersWritesBomAndHeaders() throws Exception {
    File csv = service.createEmptyCSVFileWithProviders(TABLE, 290L, headers(), false, null,
        providerFolder());
    assertArrayEquals(bomAnd("site_code\n"), Files.readAllBytes(csv.toPath()));
  }

  private byte[] export(String key) throws IOException {
    File csv = service.createCSVFile(s3Objects(key), TABLE, 290L, DatasetTypeEnum.REPORTING,
        headers(), false, null, null);
    return Files.readAllBytes(csv.toPath());
  }

  private byte[] exportWithProviders(String key) throws IOException {
    File csv = service.createCSVFileWithProviders(s3Objects(key), TABLE, 290L,
        DatasetTypeEnum.REPORTING, headers(), false, null, providerFolder());
    return Files.readAllBytes(csv.toPath());
  }

  private String providerFolder() {
    return work.resolve("provider-ES").toString();
  }

  private static List<S3Object> s3Objects(String key) {
    return Collections.singletonList(S3Object.builder().key(key).build());
  }

  private static List<String> headers() {
    return new ArrayList<>(Arrays.asList("site_code"));
  }

  private static byte[] bomAnd(String text) {
    byte[] body = text.getBytes(StandardCharsets.UTF_8);
    byte[] result = Arrays.copyOf(BOM, BOM.length + body.length);
    System.arraycopy(body, 0, result, BOM.length, body.length);
    return result;
  }

  private static Schema schema() {
    return SchemaBuilder.record("r").fields().optionalString("site_code").endRecord();
  }

  /** Writes a real parquet file with the given number of rows. */
  private Path parquet(String name, int rows) throws IOException {
    Path path = work.resolve(name);
    try (ParquetWriter<GenericRecord> writer = AvroParquetWriter
        .<GenericRecord>builder(new LocalOutputFile(path)).withSchema(schema()).build()) {
      for (int i = 0; i < rows; i++) {
        GenericRecord record = new GenericData.Record(schema());
        record.put("site_code", "ES000" + i);
        writer.write(record);
      }
    }
    return path;
  }

  /** The service deletes the downloaded parquet afterwards, so hand out a fresh copy each call. */
  private void serve(Path parquet) throws IOException {
    Path download = work.resolve("download_" + parquet.getFileName());
    when(s3Helper.getFileFromS3Export(anyString(), anyString(), anyString(), anyString(), any()))
        .thenAnswer(
            inv -> Files.copy(parquet, download, StandardCopyOption.REPLACE_EXISTING).toFile());
  }

  /** Minimal local {@link OutputFile}, so no Hadoop file system (winutils) is needed. */
  private static class LocalOutputFile implements OutputFile {

    private final Path path;

    LocalOutputFile(Path path) {
      this.path = path;
    }

    @Override
    public PositionOutputStream create(long blockSizeHint) throws IOException {
      return open();
    }

    @Override
    public PositionOutputStream createOrOverwrite(long blockSizeHint) throws IOException {
      return open();
    }

    @Override
    public boolean supportsBlockSize() {
      return false;
    }

    @Override
    public long defaultBlockSize() {
      return 0;
    }

    private PositionOutputStream open() throws IOException {
      OutputStream out = Files.newOutputStream(path);
      return new PositionOutputStream() {
        private long pos = 0;

        @Override
        public long getPos() {
          return pos;
        }

        @Override
        public void write(int b) throws IOException {
          out.write(b);
          pos++;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
          out.write(b, off, len);
          pos += len;
        }

        @Override
        public void flush() throws IOException {
          out.flush();
        }

        @Override
        public void close() throws IOException {
          out.close();
        }
      };
    }
  }
}
