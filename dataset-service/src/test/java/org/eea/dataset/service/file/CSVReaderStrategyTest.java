package org.eea.dataset.service.file;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.bson.types.ObjectId;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.persistence.schemas.domain.DataSetSchema;
import org.eea.dataset.persistence.schemas.domain.FieldSchema;
import org.eea.dataset.persistence.schemas.domain.RecordSchema;
import org.eea.dataset.persistence.schemas.domain.TableSchema;
import org.eea.dataset.service.DatasetSchemaService;
import org.eea.exception.EEAException;
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
  private DataSetSchema dataSet;

  /**
   * Inits the mocks.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
    ReflectionTestUtils.setField(csvReaderStrategy, "delimiter", ',');
    String csv = "campo_1,campo_2,campo_3\r\n" + "B,C,D\r\n" + "\"I,I\",I,I";
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", csv.getBytes());
    input = file.getInputStream();
    ArrayList<TableSchema> tables = new ArrayList<>();
    ArrayList<FieldSchema> fields = new ArrayList<>();
    dataSet = new DataSetSchema();
    dataSet.setIdDataSetSchema(new ObjectId());
    TableSchema table = new TableSchema();
    table.setNameTableSchema("tabla1");
    table.setIdTableSchema(new ObjectId("5ce524fad31fc52540abae73"));
    table.setFixedNumber(false);
    RecordSchema record = new RecordSchema();
    record.setIdRecordSchema(new ObjectId());
    FieldSchema fschema = new FieldSchema();
    fschema.setHeaderName("campo_1");
    fschema.setIdFieldSchema(new ObjectId());
    fschema.setReadOnly(false);
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
    FieldSchema fschema = new FieldSchema();
    fschema.setHeaderName("campo_1");
    fschema.setIdFieldSchema(new ObjectId());
    fschema.setReadOnly(false);
    when(fileCommon.isDesignDataset(Mockito.any())).thenReturn(false);
    // when(fileCommon.getDataSetSchema(Mockito.any(), Mockito.any())).thenReturn(dataSet);
    when(fileCommon.findIdFieldSchema(Mockito.any(), Mockito.any(),
        Mockito.any(DataSetSchema.class))).thenReturn(fschema);
    csvReaderStrategy.parseFile(input, 1L, 1L, "5ce524fad31fc52540abae73", null, null, false,
        dataSet);
    Mockito.verify(fileCommon, times(3)).findIdFieldSchema(Mockito.any(), Mockito.any(),
        Mockito.any(DataSetSchema.class));
  }


  /**
   * Test parse exception.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws EEAException
   */
  @Test(expected = InvalidFileException.class)
  public void testParseException() throws IOException, EEAException {
    String csv = "\n TABLA1,B,C,D\r\n" + "TABLA1,\"I,I\",I,I\r\n";
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", csv.getBytes());
    input = file.getInputStream();
    // when(fileCommon.getDataSetSchema(Mockito.any(), Mockito.any())).thenReturn(dataSet);
    csvReaderStrategy.parseFile(input, 1L, null, null, null, csv, false, dataSet);
  }



}
