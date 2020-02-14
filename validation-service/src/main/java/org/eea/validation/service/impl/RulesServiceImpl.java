package org.eea.validation.service.impl;


import java.util.ArrayList;
import java.util.List;
import org.bson.types.ObjectId;
import org.eea.exception.EEAException;
import org.eea.interfaces.vo.dataset.schemas.rule.RulesSchemaVO;
import org.eea.validation.mapper.RulesSchemaMapper;
import org.eea.validation.persistence.repository.ExtendedRulesRepository;
import org.eea.validation.persistence.repository.RulesRepository;
import org.eea.validation.persistence.schemas.rule.Rule;
import org.eea.validation.persistence.schemas.rule.RulesSchema;
import org.eea.validation.service.RulesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class ValidationService.
 */
@Service("RulesService")
public class RulesServiceImpl implements RulesService {

  /** The rules repository. */
  @Autowired
  private RulesRepository rulesRepository;

  /** The rules schema mapper. */
  @Autowired
  private RulesSchemaMapper rulesSchemaMapper;

  @Autowired
  private ExtendedRulesRepository extendedRulesRepository;

  /**
   * The Constant LOG_ERROR.
   */
  private static final Logger LOG_ERROR = LoggerFactory.getLogger("error_logger");

  /**
   * Gets the rules schema by dataset id.
   *
   * @param idDatasetSchema the id dataset schema
   * @return the rules schema by dataset id
   */
  @Override
  public RulesSchemaVO getRulesSchemaByDatasetId(String idDatasetSchema) {
    return rulesSchemaMapper
        .entityToClass(rulesRepository.findByIdDatasetSchema(new ObjectId(idDatasetSchema)));
  }

  /**
   * Creates the empty rules scehma.
   *
   * @param schemaId the schema id
   * @param ruleSchemaId the rule schema id
   */
  @Override
  public void createEmptyRulesSchema(ObjectId schemaId, ObjectId ruleSchemaId) {
    RulesSchema rSchema = new RulesSchema();
    rSchema.setIdDatasetSchema(schemaId);
    rSchema.setRulesSchemaId(ruleSchemaId);
    List<Rule> ruleList = new ArrayList<>();
    rSchema.setRules(ruleList);

    rulesRepository.save(rSchema);
  }

  /**
   * Delete empty rules scehma.
   *
   * @param schemaId the schema id
   */
  @Override
  public void deleteEmptyRulesScehma(ObjectId schemaId) {

    RulesSchema ruleSchema = rulesRepository.findByIdDatasetSchema(schemaId);
    System.out.println("este es el: " + ruleSchema);
    if (null != ruleSchema) {
      System.out.println("No es null y voy a borarlo");
      extendedRulesRepository.deleteByIdDatasetSchema(ruleSchema.getRulesSchemaId());
    }
  }

  /**
   * Delete rule by id.
   *
   * @param idDatasetSchema the id dataset schema
   * @param ruleId the rule id
   * @throws EEAException the EEA exception
   */
  @Override
  public void deleteRuleById(String idDatasetSchema, String ruleId) throws EEAException {
    // rulesRepository.deleteRuleById(ruleId);
  }

  @Override
  public void deleteRuleByReferenceId(String idDatasetSchema, String referenceId)
      throws EEAException {
    // rulesRepository.deleteRuleByReferenceId(referenceId);

  }
}
