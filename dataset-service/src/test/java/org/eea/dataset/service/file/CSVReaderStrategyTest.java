package org.eea.dataset.service.file;

import static org.mockito.Mockito.when;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.eea.dataset.exception.InvalidFileException;
import org.eea.dataset.service.DatasetSchemaService;
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

@RunWith(MockitoJUnitRunner.class)
public class CSVReaderStrategyTest {

  @InjectMocks
  CSVReaderStrategy csvReaderStrategy;

  InputStream input;

  @Mock
  DatasetSchemaService datasetSchemaService;


  DataSetSchemaVO dataSet;

  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
    String csv =
        "\n_table|campo_1|campo_2|campo_3\r\n" + "TABLA1|B|C|D\r\n" + "TABLA1|\"I|I\"|I|I\r\n"
            + "_table|campo_11|campo_12\r\n" + "TABLA3|J|K\r\n" + "TABLA3|M|N";
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", csv.getBytes());
    input = file.getInputStream();
    ArrayList<TableSchemaVO> tables = new ArrayList<>();
    ArrayList<FieldSchemaVO> fields = new ArrayList<>();
    dataSet = new DataSetSchemaVO();
    TableSchemaVO table = new TableSchemaVO();
    table.setNameTableSchema("tabla1");
    table.setIdTableSchema("-");
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


  @Test
  public void testParseFile() throws InvalidFileException {
    when(datasetSchemaService.getDataSchemaByIdFlow(Mockito.anyLong())).thenReturn(dataSet);
    csvReaderStrategy.parseFile(input, Mockito.anyLong(), null);
  }

  @Test
  public void testParseFileTableNull() throws InvalidFileException {
    dataSet.setTableSchemas(null);
    when(datasetSchemaService.getDataSchemaByIdFlow(Mockito.anyLong())).thenReturn(dataSet);
    csvReaderStrategy.parseFile(input, Mockito.anyLong(), null);
  }


  @Test(expected = InvalidFileException.class)
  public void testParseException() throws InvalidFileException, IOException {
    String csv = "TABLA1|B|C|D\r\n" + "TABLA1|\"I|I\"|I|I\r\n";
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", csv.getBytes());
    input = file.getInputStream();
    csvReaderStrategy.parseFile(input, Mockito.anyLong(), null);
  }


}
