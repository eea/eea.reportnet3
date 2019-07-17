package org.eea.dataset.service.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.persistence.data.domain.FieldValue;
import org.eea.dataset.persistence.data.domain.RecordValue;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * The Class CSVWriterStrategyTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class CSVWriterStrategyTest {

  /** The csv writer strategy. */
  @InjectMocks
  private CSVWriterStrategy csvWriterStrategy;

  /** The parse common. */
  @Mock
  private FileCommonUtils fileCommon;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    ReflectionTestUtils.setField(csvWriterStrategy, "delimiter", '|');
  }

  /**
   * Csv writer strategy test.
   */
  @Test
  public void csvWriterStrategyTest() {
    CSVWriterStrategy test = new CSVWriterStrategy('|', fileCommon);
  }


  /**
   * Test write file.
   *
   * @throws InvalidFileException the invalid file exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void testWriteFile() throws InvalidFileException, IOException {
    List<RecordValue> records = new ArrayList<>();
    List<FieldSchemaVO> fieldSchemas = new ArrayList<>();
    List<FieldValue> fields = new ArrayList<>();
    RecordValue record = new RecordValue();
    FieldValue fieldValue = new FieldValue();
    FieldSchemaVO fieldSchema = new FieldSchemaVO();
    fieldSchema.setId("");
    fieldValue.setIdFieldSchema("");
    fields.add(fieldValue);
    record.setFields(fields);
    records.add(record);
    fieldSchemas.add(fieldSchema);
    Mockito.when(fileCommon.getRecordValues(Mockito.any(), Mockito.any())).thenReturn(records);
    Mockito.when(fileCommon.getFieldSchemas(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchemas);
    csvWriterStrategy.writeFile(1L, 1L, "");
  }

  /**
   * Test write file write.
   *
   * @throws InvalidFileException the invalid file exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Test
  public void testWriteFileWrite() throws InvalidFileException, IOException {
    List<RecordValue> records = new ArrayList<>();
    List<FieldSchemaVO> fieldSchemas = new ArrayList<>();
    List<FieldValue> fields = new ArrayList<>();
    RecordValue record = new RecordValue();
    RecordValue record2 = new RecordValue();
    FieldValue fieldValue = new FieldValue();
    FieldSchemaVO fieldSchema = new FieldSchemaVO();
    fieldSchema.setId("");
    fields.add(fieldValue);
    record.setFields(fields);
    record2.setFields(new ArrayList<>());
    records.add(record);
    records.add(record2);
    fieldSchemas.add(fieldSchema);
    Mockito.when(fileCommon.getRecordValues(Mockito.any(), Mockito.any())).thenReturn(records);
    Mockito.when(fileCommon.getFieldSchemas(Mockito.any(), Mockito.any()))
        .thenReturn(fieldSchemas);
    csvWriterStrategy.writeFile(1L, 1L, "");
  }
}
