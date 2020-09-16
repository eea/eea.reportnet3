package org.eea.validation.service;

import org.eea.validation.persistence.data.domain.TableValue;
import org.eea.validation.persistence.schemas.rule.Rule;

public interface SqlRulesService {

  void validateSQLRule(String datasetSchemaId, Rule rule);

  String queryTreat(String query);

  Rule getRule(Long datasetId, String ruleId);

  TableValue retrivedata(String query);

}
