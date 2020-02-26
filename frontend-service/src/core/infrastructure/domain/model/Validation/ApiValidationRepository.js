import { isEmpty, isNull, isUndefined } from 'lodash';

import { apiValidation } from 'core/infrastructure/api/domain/model/Validation';

const deleteById = async (datasetSchemaId, ruleId) => {
  return await apiValidation.deleteById(datasetSchemaId, ruleId);
};

const getAll = async datasetSchemaId => {
  const validationsListDTO = await apiValidation.getAll(datasetSchemaId);
  if (isUndefined(validationsListDTO) || isEmpty(validationsListDTO.rules)) {
    return;
  }

  const validationsList = {};
  validationsList.datasetSchemaId = validationsListDTO.idDatasetSchema;
  validationsList.rulesSchemaId = validationsListDTO.rulesSchemaId;

  const rulesData = parseDataValidationRulesDTO(validationsListDTO.rules);
  validationsList.entityTypes = rulesData.entityTypes;
  validationsList.rules = rulesData.rules;
  return validationsList;
};

const parseDataValidationRulesDTO = rulesDTO => {
  const rulesData = {};
  const entityTypes = [];

  rulesData.rules = rulesDTO.map(ruleDTO => {
    entityTypes.push(ruleDTO.type);

    const rule = {};
    rule.shortCode = ruleDTO.shortCode;
    rule.name = ruleDTO.ruleName;
    rule.description =
      !isUndefined(ruleDTO.thenCondition) && !isNull(ruleDTO.thenCondition[0]) ? ruleDTO.thenCondition[0] : null;
    rule.enabled = ruleDTO.enabled;
    rule.automatic = ruleDTO.automatic;
    rule.order = ruleDTO.order;
    rule.entityType = ruleDTO.type;
    rule.message =
      !isUndefined(ruleDTO.thenCondition) && !isNull(ruleDTO.thenCondition[0]) ? ruleDTO.thenCondition[0] : null;
    rule.levelError =
      !isUndefined(ruleDTO.thenCondition) && !isNull(ruleDTO.thenCondition[1]) ? ruleDTO.thenCondition[1] : null;
    return rule;
  });

  rulesData.entityTypes = [...new Set(entityTypes)];
  return rulesData;
};
export const ApiValidationRepository = {
  deleteById,
  getAll
};
