package org.eea.validation.util;

import static org.mockito.Mockito.when;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.DataSetSchema;
import org.eea.validation.persistence.schemas.FieldSchema;
import org.eea.validation.persistence.schemas.RecordSchema;
import org.eea.validation.persistence.schemas.TableSchema;
import org.eea.validation.persistence.schemas.rule.RuleDataSet;
import org.eea.validation.persistence.schemas.rule.RuleField;
import org.eea.validation.persistence.schemas.rule.RuleRecord;
import org.eea.validation.persistence.schemas.rule.RuleTable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class KieBaseManagerTest {

  @InjectMocks
  private KieBaseManager kieBaseManager;
  @Mock
  private SchemasRepository schemasRepository;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testKieBaseManager() throws FileNotFoundException {
    DataSetSchema dataSchema = new DataSetSchema();
    // LIST STRINGS
    List<String> listString = new ArrayList<String>();
    listString.add("ERROR VALIDATION");
    listString.add("ERROR");
    // RULES DATASET
    List<RuleDataSet> ruleDataSetList = new ArrayList<RuleDataSet>();
    RuleDataSet ruleDataset = new RuleDataSet();
    ruleDataset.setIdDataSetSchema(new ObjectId());
    ruleDataset.setRuleId(new ObjectId());
    ruleDataset.setRuleName("regla dataset");
    ruleDataset.setWhenCondition("id == null");
    ruleDataset.setThenCondition(listString);
    ruleDataSetList.add(ruleDataset);
    // RULE TABLE
    List<RuleTable> ruleTableList = new ArrayList<RuleTable>();
    RuleTable ruleTable = new RuleTable();
    ruleTable.setIdTableSchema(new ObjectId());
    ruleTable.setRuleId(new ObjectId());
    ruleTable.setRuleName("regla tabñe");
    ruleTable.setWhenCondition("id == null");
    ruleTable.setThenCondition(listString);
    ruleTableList.add(ruleTable);

    // RULES RECORDS
    List<RuleRecord> ruleRecordList = new ArrayList<RuleRecord>();
    RuleRecord ruleRecord = new RuleRecord();
    ruleRecord.setIdRecordSchema(new ObjectId());
    ruleRecord.setRuleId(new ObjectId());
    ruleRecord.setRuleName("regla record");
    ruleRecord.setWhenCondition("id == null");
    ruleRecord.setThenCondition(listString);
    ruleRecordList.add(ruleRecord);

    // RULES FIELDS
    List<RuleField> ruleFieldList = new ArrayList<RuleField>();
    ruleDataset.setIdDataSetSchema(new ObjectId());
    RuleField ruleField = new RuleField();
    ruleField.setIdFieldSchema(new ObjectId());
    ruleField.setRuleId(new ObjectId());
    ruleField.setRuleName("regla field");
    ruleField.setWhenCondition("id == null");
    ruleField.setThenCondition(listString);
    ruleFieldList.add(ruleField);

    // PART TO COMPONT THE OBJET TO RETURN
    List<TableSchema> tableSchemasList = new ArrayList<TableSchema>();
    TableSchema tableSchema = new TableSchema();
    RecordSchema record = new RecordSchema();
    List<FieldSchema> fieldSchemaList = new ArrayList<FieldSchema>();
    FieldSchema fieldSchema = new FieldSchema();
    fieldSchema.setRuleField(ruleFieldList);
    fieldSchemaList.add(fieldSchema);
    record.setFieldSchema(fieldSchemaList);
    record.setRuleRecord(ruleRecordList);
    tableSchema.setRecordSchema(record);
    tableSchema.setRuleTable(ruleTableList);
    tableSchema.setNameTableSchema("paco");
    tableSchemasList.add(tableSchema);
    dataSchema.setRuleDataSet(ruleDataSetList);
    dataSchema.setTableSchemas(tableSchemasList);
    // CALL SERVICES
    when(schemasRepository.findSchemaByIdFlow(Mockito.any())).thenReturn(dataSchema);
    kieBaseManager.reloadRules(1L);
  }

}
