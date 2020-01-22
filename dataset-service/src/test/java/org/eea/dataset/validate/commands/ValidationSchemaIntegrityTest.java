package org.eea.dataset.validate.commands;

import static org.junit.Assert.assertFalse;
import java.util.ArrayList;
import org.eea.interfaces.vo.dataset.schemas.DataSetSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.RecordSchemaVO;
import org.eea.interfaces.vo.dataset.schemas.TableSchemaVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class ValidationSchemaIntegrityTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationSchemaIntegrityTest {

  /** The validation integrity. */
  @InjectMocks
  private ValidationSchemaIntegrityCommand validationIntegrity;


  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }


  @Test
  public void testExecute() {
    DataSetSchemaVO schema = new DataSetSchemaVO();
    validationIntegrity.execute(schema);
    assertFalse("its valid", validationIntegrity.execute(schema));
  }


  @Test
  public void testExecute2() {
    DataSetSchemaVO schema = new DataSetSchemaVO();
    schema.setTableSchemas(new ArrayList<>());
    TableSchemaVO table = new TableSchemaVO();
    schema.getTableSchemas().add(table);
    assertFalse("its valid", validationIntegrity.execute(schema));
  }

  @Test
  public void testExecute3() {
    DataSetSchemaVO schema = new DataSetSchemaVO();
    schema.setTableSchemas(new ArrayList<>());
    TableSchemaVO table = new TableSchemaVO();
    RecordSchemaVO record = new RecordSchemaVO();
    table.setRecordSchema(record);
    schema.getTableSchemas().add(table);
    assertFalse("its valid", validationIntegrity.execute(schema));
  }

  @Test
  public void testExecute4() {
    DataSetSchemaVO schema = new DataSetSchemaVO();
    schema.setTableSchemas(new ArrayList<>());
    TableSchemaVO table = new TableSchemaVO();
    RecordSchemaVO record = new RecordSchemaVO();
    record.setFieldSchema(new ArrayList<>());
    table.setRecordSchema(record);
    schema.getTableSchemas().add(table);
    assertFalse("its valid", validationIntegrity.execute(schema));
  }


}
