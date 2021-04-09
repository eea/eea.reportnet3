package org.eea.dataset.service.file;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.DataSetVO;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.FieldSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

/*** The Class CSVReaderStrategyTest. */

@RunWith(MockitoJUnitRunner.class)
public class CSVReaderStrategyTest {

  /** The csv reader strategy. */
  @InjectMocks
  private CSVReaderStrategy csvReaderStrategy;

  /** The parse common. */
  @Mock
  private FileCommonUtils fileCommon;


  /** The input. */
  private InputStream input;

  /** The dataset schema service. */
  @Mock
  private DatasetSchemaService datasetSchemaService;


  /** The data set. */
  private DataSetSchemaVO dataSet;

  /**
   * Inits the mocks.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
    ReflectionTestUtils.setField(csvReaderStrategy, "delimiter", '|');
    String csv = "campo_1|campo_2|campo_3\r\n" + "B|C|D\r\n" + "\"I|I\"|I|I";
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", csv.getBytes());
    input = file.getInputStream();
    ArrayList<TableSchemaVO> tables = new ArrayList<>();
    ArrayList<FieldSchemaVO> fields = new ArrayList<>();
    dataSet = new DataSetSchemaVO();
    TableSchemaVO table = new TableSchemaVO();
    table.setNameTableSchema("tabla1");
    table.setIdTableSchema("-");
    table.setFixedNumber(false);
    RecordSchemaVO record = new RecordSchemaVO();
    record.setIdRecordSchema("");
    FieldSchemaVO fschema = new FieldSchemaVO();
    fschema.setName("campo_1");
    fschema.setId("");
    fields.add(fschema);
    record.setFieldSchema(fields);
    table.setRecordSchema(record);
    tables.add(table);
    dataSet.setTableSchemas(tables);


  }


  /**
   * Test parse file.
   *
   * @throws EEAException
   */
  @Test
  public void testParseFile() throws EEAException {

    when(fileCommon.getDataSetSchema(Mockito.any(), Mockito.any())).thenReturn(dataSet);
    when(fileCommon.findIdFieldSchema(Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(new FieldSchemaVO());
    DataSetVO result = csvReaderStrategy.parseFile(input, 1L, 1L, "-");
    assertNotNull("is null", result);
  }


  /**
   * Test parse exception.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException
   */
  @Test(expected = InvalidFileException.class)
  public void testParseException() throws IOException, EEAException {
    String csv = "\n TABLA1|B|C|D\r\n" + "TABLA1|\"I|I\"|I|I\r\n";
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", csv.getBytes());
    input = file.getInputStream();
    when(fileCommon.getDataSetSchema(Mockito.any(), Mockito.any())).thenReturn(dataSet);
    csvReaderStrategy.parseFile(input, 1L, null, null);
  }



}
