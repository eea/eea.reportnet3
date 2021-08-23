package org.eea.validation.persistence.data.repository;

import static org.mockito.Mockito.times;
import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.validation.persistence.repository.ExtendedRulesRepositoryImpl;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import com.mongodb.client.result.UpdateResult;

/**
 * The Class ExtendedRulesRepositoryImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExtendedRulesRepositoryImplTest {

  /** The extended rules repository impl. */
  @InjectMocks
  private ExtendedRulesRepositoryImpl extendedRulesRepositoryImpl;

  /** The mongo template. */
  @Mock
  private MongoTemplate mongoTemplate;

  /** The aggregation results. */
  @Mock
  private AggregationResults<Object> aggregationResults;

  /**
   * Inits the mocks.
   */
  @Before
  public void initMocks() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Delete by id dataset schema test.
   */
  @Test
  public void deleteByIdDatasetSchemaTest() {
    Mockito.when(mongoTemplate.findAndRemove(Mockito.any(), Mockito.any())).thenReturn(null);
    extendedRulesRepositoryImpl.deleteByIdDatasetSchema(new ObjectId());
    Mockito.verify(mongoTemplate, times(1)).findAndRemove(Mockito.any(), Mockito.any());
  }

  /**
   * Delete rule by id true test.
   */
  @Test
  public void deleteRuleByIdTrueTest() {
    Mockito.when(mongoTemplate.updateFirst(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert.assertTrue(extendedRulesRepositoryImpl.deleteRuleById(new ObjectId(), new ObjectId()));
  }

  /**
   * Delete rule by id false test.
   */
  @Test
  public void deleteRuleByIdFalseTest() {
    Mockito.when(mongoTemplate.updateFirst(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 0L, null));
    Assert.assertFalse(extendedRulesRepositoryImpl.deleteRuleById(new ObjectId(), new ObjectId()));
  }

  /**
   * Delete rule by reference id true test.
   */
  @Test
  public void deleteRuleByReferenceIdTrueTest() {
    Mockito.when(mongoTemplate.updateMulti(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert.assertTrue(
        extendedRulesRepositoryImpl.deleteRuleByReferenceId(new ObjectId(), new ObjectId()));
  }

  /**
   * Delete rule by reference id false test.
   */
  @Test
  public void deleteRuleByReferenceIdFalseTest() {
    Mockito.when(mongoTemplate.updateMulti(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 0L, null));
    Assert.assertFalse(
        extendedRulesRepositoryImpl.deleteRuleByReferenceId(new ObjectId(), new ObjectId()));
  }

  /**
   * Delete rule required true test.
   */
  @Test
  public void deleteRuleRequiredTrueTest() {
    Mockito.when(mongoTemplate.updateFirst(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert
        .assertTrue(extendedRulesRepositoryImpl.deleteRuleRequired(new ObjectId(), new ObjectId()));
  }

  /**
   * Delete rule required false test.
   */
  @Test
  public void deleteRuleRequiredFalseTest() {
    Mockito.when(mongoTemplate.updateFirst(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 0L, null));
    Assert.assertFalse(
        extendedRulesRepositoryImpl.deleteRuleRequired(new ObjectId(), new ObjectId()));
  }

  /**
   * Creates the new rule true test.
   */
  @Test
  public void createNewRuleTrueTest() {
    Mockito.when(mongoTemplate.updateMulti(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert.assertTrue(extendedRulesRepositoryImpl.createNewRule(new ObjectId(), new Rule()));
  }

  /**
   * Creates the new rule false test.
   */
  @Test
  public void createNewRuleFalseTest() {
    Mockito.when(mongoTemplate.updateMulti(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 0L, null));
    Assert.assertFalse(extendedRulesRepositoryImpl.createNewRule(new ObjectId(), new Rule()));
  }

  /**
   * Update rule true test.
   */
  @Test
  public void updateRuleTrueTest() {
    Mockito.when(mongoTemplate.updateFirst(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert.assertTrue(extendedRulesRepositoryImpl.updateRule(new ObjectId(), new Rule()));
  }

  /**
   * Update rule false test.
   */
  @Test
  public void updateRuleFalseTest() {
    Mockito.when(mongoTemplate.updateFirst(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(0L, 0L, null));
    Assert.assertFalse(extendedRulesRepositoryImpl.updateRule(new ObjectId(), new Rule()));
  }

  /**
   * Exists rule required true test.
   */
  @Test
  public void existsRuleRequiredTrueTest() {
    Mockito.when(mongoTemplate.count(Mockito.any(), Mockito.any(Class.class))).thenReturn(1L);
    Assert
        .assertTrue(extendedRulesRepositoryImpl.existsRuleRequired(new ObjectId(), new ObjectId()));
  }

  /**
   * Exists rule required false test.
   */
  @Test
  public void existsRuleRequiredFalseTest() {
    Mockito.when(mongoTemplate.count(Mockito.any(), Mockito.any(Class.class))).thenReturn(0L);
    Assert.assertFalse(
        extendedRulesRepositoryImpl.existsRuleRequired(new ObjectId(), new ObjectId()));
  }

  /**
   * Insert rule in position true test.
   */
  @Test
  public void insertRuleInPositionTrueTest() {
    Mockito.when(mongoTemplate.updateFirst(Mockito.any(), Mockito.any(), Mockito.any(Class.class),
        Mockito.any())).thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert.assertTrue(
        extendedRulesRepositoryImpl.insertRuleInPosition(new ObjectId(), new Rule(), 1));
  }

  /**
   * Insert rule in position false test.
   */
  @Test
  public void insertRuleInPositionFalseTest() {
    Mockito.when(mongoTemplate.updateFirst(Mockito.any(), Mockito.any(), Mockito.any(Class.class),
        Mockito.any())).thenReturn(UpdateResult.acknowledged(1L, 0L, null));
    Assert.assertFalse(
        extendedRulesRepositoryImpl.insertRuleInPosition(new ObjectId(), new Rule(), 1));
  }

  /**
   * Find rule test.
   */
  @Test
  @SuppressWarnings("unchecked")
  public void findRuleTest() {
    Rule rule = new Rule();
    List<Rule> rules = new ArrayList<>();
    rules.add(rule);
    RulesSchema rulesSchema = new RulesSchema();
    rulesSchema.setRules(rules);
    Mockito.when(
        mongoTemplate.aggregate(Mockito.any(), Mockito.any(Class.class), Mockito.any(Class.class)))
        .thenReturn(aggregationResults);
    Mockito.when(aggregationResults.getUniqueMappedResult()).thenReturn(rulesSchema);
    Assert.assertEquals(rule, extendedRulesRepositoryImpl.findRule(new ObjectId(), new ObjectId()));
  }

  /**
   * Find rule null test.
   */
  @Test
  @SuppressWarnings("unchecked")
  public void findRuleNullTest() {
    Mockito.when(
        mongoTemplate.aggregate(Mockito.any(), Mockito.any(Class.class), Mockito.any(Class.class)))
        .thenReturn(aggregationResults);
    Mockito.when(aggregationResults.getUniqueMappedResult()).thenReturn(null);
    Assert.assertNull(extendedRulesRepositoryImpl.findRule(new ObjectId(), new ObjectId()));
  }

  /**
   * Find rule null rules test.
   */
  @Test
  @SuppressWarnings("unchecked")
  public void findRuleNullRulesTest() {
    Mockito.when(
        mongoTemplate.aggregate(Mockito.any(), Mockito.any(Class.class), Mockito.any(Class.class)))
        .thenReturn(aggregationResults);
    Mockito.when(aggregationResults.getUniqueMappedResult()).thenReturn(new RulesSchema());
    Assert.assertNull(extendedRulesRepositoryImpl.findRule(new ObjectId(), new ObjectId()));
  }

  /**
   * Find rule empty rules test.
   */
  @Test
  @SuppressWarnings("unchecked")
  public void findRuleEmptyRulesTest() {
    RulesSchema rulesSchema = new RulesSchema();
    rulesSchema.setRules(new ArrayList<Rule>());
    Mockito.when(
        mongoTemplate.aggregate(Mockito.any(), Mockito.any(Class.class), Mockito.any(Class.class)))
        .thenReturn(aggregationResults);
    Mockito.when(aggregationResults.getUniqueMappedResult()).thenReturn(rulesSchema);
    Assert.assertNull(extendedRulesRepositoryImpl.findRule(new ObjectId(), new ObjectId()));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void getRulesWithActiveCriteriaEmptyListTest() {
    Mockito.when(mongoTemplate.find(Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(new ArrayList<RulesSchema>());
    Assert
        .assertNull(extendedRulesRepositoryImpl.getRulesWithActiveCriteria(new ObjectId(), false));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void getRulesWithActiveCriteriaTest() {
    RulesSchema rulesSchema = new RulesSchema();
    List<Object> rulesSchemas = new ArrayList<>();
    rulesSchemas.add(rulesSchema);
    Mockito.when(
        mongoTemplate.aggregate(Mockito.any(), Mockito.any(Class.class), Mockito.any(Class.class)))
        .thenReturn(aggregationResults);
    Mockito.when(aggregationResults.getMappedResults()).thenReturn(rulesSchemas);
    Assert.assertEquals(rulesSchema,
        extendedRulesRepositoryImpl.getRulesWithActiveCriteria(new ObjectId(), true));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void getRulesWithTypeRuleCriteriaNullTest() {
    Mockito.when(
        mongoTemplate.aggregate(Mockito.any(), Mockito.any(Class.class), Mockito.any(Class.class)))
        .thenReturn(aggregationResults);
    Mockito.when(aggregationResults.getMappedResults()).thenReturn(new ArrayList<>());
    Assert.assertNull(
        extendedRulesRepositoryImpl.getRulesWithTypeRuleCriteria(new ObjectId(), false));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void getRulesWithTypeRuleCriteriaTest() {
    RulesSchema rulesSchema = new RulesSchema();
    List<Object> rulesSchemas = new ArrayList<>();
    rulesSchemas.add(rulesSchema);
    Mockito.when(
        mongoTemplate.aggregate(Mockito.any(), Mockito.any(Class.class), Mockito.any(Class.class)))
        .thenReturn(aggregationResults);
    Mockito.when(aggregationResults.getMappedResults()).thenReturn(rulesSchemas);
    Assert.assertEquals(rulesSchema,
        extendedRulesRepositoryImpl.getRulesWithTypeRuleCriteria(new ObjectId(), true));
  }

  @Test
  public void deleteRuleByReferenceFieldSchemaPKIdTrueTest() {
    Mockito.when(mongoTemplate.updateMulti(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert.assertTrue(extendedRulesRepositoryImpl
        .deleteRuleByReferenceFieldSchemaPKId(new ObjectId(), new ObjectId()));
  }

  @Test
  public void deleteRuleByReferenceFieldSchemaPKIdFalseTest() {
    Mockito.when(mongoTemplate.updateMulti(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 0L, null));
    Assert.assertFalse(extendedRulesRepositoryImpl
        .deleteRuleByReferenceFieldSchemaPKId(new ObjectId(), new ObjectId()));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void getActiveAndVerifiedRulesTest() {
    RulesSchema rulesSchema = new RulesSchema();
    List<Object> rulesSchemas = new ArrayList<>();
    rulesSchemas.add(rulesSchema);
    Mockito.when(
        mongoTemplate.aggregate(Mockito.any(), Mockito.any(Class.class), Mockito.any(Class.class)))
        .thenReturn(aggregationResults);
    Mockito.when(aggregationResults.getMappedResults()).thenReturn(rulesSchemas);
    Assert.assertEquals(rulesSchema,
        extendedRulesRepositoryImpl.getActiveAndVerifiedRules(new ObjectId()));
  }

  @Test
  public void deleteByUniqueConstraintIdTest() {
    Mockito.when(mongoTemplate.updateMulti(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 0L, null));
    Assert.assertEquals(false,
        extendedRulesRepositoryImpl.deleteByUniqueConstraintId(new ObjectId(), new ObjectId()));
  }

  @Test
  public void deleteRuleHighLevelLikeTrueTest() {
    Mockito.when(mongoTemplate.updateMulti(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert.assertTrue(extendedRulesRepositoryImpl.deleteRuleHighLevelLike(new ObjectId(), ""));
  }

  @Test
  public void deleteRuleHighLevelLikeFalseTest() {
    Mockito.when(mongoTemplate.updateMulti(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 0L, null));
    Assert.assertFalse(extendedRulesRepositoryImpl.deleteRuleHighLevelLike(new ObjectId(), ""));
  }

  @Test
  public void deleteNotEmptyRuleTrueTest() {
    Mockito.when(mongoTemplate.updateFirst(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert
        .assertTrue(extendedRulesRepositoryImpl.deleteNotEmptyRule(new ObjectId(), new ObjectId()));
  }

  @Test
  public void deleteNotEmptyRuleFalseTest() {
    Mockito.when(mongoTemplate.updateFirst(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 0L, null));
    Assert.assertFalse(
        extendedRulesRepositoryImpl.deleteNotEmptyRule(new ObjectId(), new ObjectId()));
  }


  @Test
  public void emptyRulesSchemaByDatasetSchemaIdTest() {
    Mockito.when(mongoTemplate.updateMulti(Mockito.any(), Mockito.any(), Mockito.any(Class.class)))
        .thenReturn(UpdateResult.acknowledged(1L, 1L, null));
    Assert.assertTrue(
        extendedRulesRepositoryImpl.emptyRulesOfSchemaByDatasetSchemaId(new ObjectId()));
  }
}
