package org.eea.validation.util;

import static org.mockito.Mockito.when;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eea.interfaces.controller.dataset.DatasetMetabaseController;
import org.eea.interfaces.vo.dataset.DataSetMetabaseVO;
import org.eea.interfaces.vo.dataset.enums.EntityTypeEnum;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.repository.SchemasRepository;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * The Class KieBaseManagerTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class KieBaseManagerTest {

  /** The kie base manager. */
  @InjectMocks
  private KieBaseManager kieBaseManager;

  /** The rules repository. */
  @Mock
  private RulesRepository rulesRepository;

  /** The schemas repository. */
  @Mock
  private SchemasRepository schemasRepository;

  /** The dataset metabase controller. */
  @Mock
  private DatasetMetabaseController datasetMetabaseController;


  private RulesSchema rulesSchemas;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {

    rulesSchemas = new RulesSchema();
    // LIST STRINGS
    List<String> listString = new ArrayList<>();
    listString.add("ERROR VALIDATION");
    listString.add("ERROR");
    // RULES DATASET
    List<Rule> ruleKiebase = new ArrayList<>();
    Rule ruleDataset = new Rule();
    ruleDataset.setReferenceId(new ObjectId());
    ruleDataset.setRuleId(new ObjectId());
    ruleDataset.setRuleName("regla dataset");
    ruleDataset.setEnabled(Boolean.TRUE);
    ruleDataset.setType(EntityTypeEnum.DATASET);
    ruleDataset.setWhenCondition("id == null");
    ruleDataset.setThenCondition(listString);
    ruleKiebase.add(ruleDataset);
    // RULE TABLE

    Rule ruleTable = new Rule();
    ruleTable.setReferenceId(new ObjectId());
    ruleTable.setRuleId(new ObjectId());
    ruleTable.setRuleName("regla tadasñe");
    ruleTable.setEnabled(Boolean.TRUE);
    ruleTable.setType(EntityTypeEnum.TABLE);
    ruleTable.setWhenCondition("id == null");
    ruleTable.setThenCondition(listString);
    ruleKiebase.add(ruleTable);

    // RULES RECORDS
    Rule ruleRecord = new Rule();
    ruleRecord.setReferenceId(new ObjectId());
    ruleRecord.setRuleId(new ObjectId());
    ruleRecord.setRuleName("regla recordasda");
    ruleRecord.setEnabled(Boolean.TRUE);
    ruleRecord.setType(EntityTypeEnum.RECORD);
    ruleRecord.setWhenCondition("id == null");
    ruleRecord.setThenCondition(listString);
    ruleKiebase.add(ruleRecord);

    // RULES FIELDS
    ruleDataset.setReferenceId(new ObjectId());
    Rule ruleField = new Rule();
    ruleField.setReferenceId(new ObjectId());
    ruleField.setRuleId(new ObjectId());
    ruleField.setRuleName("regla field");
    ruleField.setEnabled(Boolean.TRUE);
    ruleField.setAutomatic(false);
    ruleField.setType(EntityTypeEnum.FIELD);
    ruleField.setWhenCondition("id == null");
    ruleField.setThenCondition(listString);
    ruleKiebase.add(ruleField);

    rulesSchemas.setRules(ruleKiebase);
    MockitoAnnotations.initMocks(this);
  }

  /**
   * Test kie base manager.
   *
   * @throws FileNotFoundException the file not found exception
   */
  @Test()
  public void testKieBaseManager() throws FileNotFoundException {

    ObjectId idObject = new ObjectId();
    // CALL SERVICES
    DataSetMetabaseVO dataSetMetabaseVO = new DataSetMetabaseVO();
    when(datasetMetabaseController.findDatasetMetabaseById(1L)).thenReturn(dataSetMetabaseVO);
    when(rulesRepository.getRulesWithActiveCriteria(idObject, true)).thenReturn(rulesSchemas);
    Document doc = new Document();
    doc.put("typeData", "DATE");
    when(schemasRepository.findFieldSchema(Mockito.any(), Mockito.any())).thenReturn(doc);

    kieBaseManager.reloadRules(1L, idObject.toString());
  }


  /**
   * Test kie base manager null.
   *
   * @throws FileNotFoundException the file not found exception
   */
  @Test()
  public void testKieBaseManagerNull() throws FileNotFoundException {
    RulesSchema rulesSchemas = new RulesSchema();
    rulesSchemas.setRules(null);
    ObjectId idObject = new ObjectId();
    // CALL SERVICES
    DataSetMetabaseVO dataSetMetabaseVO = new DataSetMetabaseVO();
    when(datasetMetabaseController.findDatasetMetabaseById(1L)).thenReturn(dataSetMetabaseVO);
    when(rulesRepository.getRulesWithActiveCriteria(idObject, true)).thenReturn(rulesSchemas);
    kieBaseManager.reloadRules(1L, idObject.toString());
  }
}
