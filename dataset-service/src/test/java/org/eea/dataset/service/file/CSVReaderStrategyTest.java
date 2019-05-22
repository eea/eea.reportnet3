package org.eea.dataset.service.file;

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
import org.eea.dataset.persistence.schemas.repository.SchemasRepository;
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

  // @Mock
  InputStream input;

  @Mock
  SchemasRepository schemasRepository;

  ArrayList<DataSetSchema> dataSets;

  @Before
  public void initMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
    String csv =
        "\n_table|campo_1|campo_2|campo_3\r\n" + "TABLA1|B|C|D\r\n" + "TABLA1|\"I|I\"|I|I\r\n"
            + "_table|campo_11|campo_12\r\n" + "TABLA3|J|K\r\n" + "TABLA3|M|N";
    MockMultipartFile file =
        new MockMultipartFile("file", "fileOriginal.csv", "cvs", csv.getBytes());
    input = file.getInputStream();
    dataSets = new ArrayList<DataSetSchema>();
    ArrayList<TableSchema> tables = new ArrayList<>();
    ArrayList<FieldSchema> fields = new ArrayList<>();
    DataSetSchema dataSet = new DataSetSchema();
    TableSchema table = new TableSchema();
    table.setNameTableSchema("tabla1");
    table.setIdTableSchema(new ObjectId());
    RecordSchema record = new RecordSchema();
    record.setIdRecordSchema(new ObjectId());
    FieldSchema fschema = new FieldSchema();
    fschema.setHeaderName("campo_1");
    fschema.setIdFieldSchema(new ObjectId());
    fields.add(fschema);
    record.setFieldSchema(fields);
    table.setRecordSchema(record);
    tables.add(table);
    dataSet.setTableSchemas(tables);
    dataSets.add(dataSet);


  }


  @Test
  @Ignore
  public void testParseFile() throws InvalidFileException {
    when(schemasRepository.findByIdDataFlow(Mockito.anyLong())).thenReturn(dataSets);
    csvReaderStrategy.parseFile(input, Mockito.anyLong(), null);
  }

  @Test
  public void testParseFileNullDataFlow() throws InvalidFileException {
    when(schemasRepository.findByIdDataFlow(Mockito.anyLong())).thenReturn(dataSets);
    csvReaderStrategy.parseFile(input, null, null);
  }


  @Test
  public void testParseFileNullDataSetSchema() throws InvalidFileException {
    when(schemasRepository.findByIdDataFlow(Mockito.anyLong())).thenReturn(new ArrayList<>());
    csvReaderStrategy.parseFile(input, Mockito.anyLong(), null);
  }

}
