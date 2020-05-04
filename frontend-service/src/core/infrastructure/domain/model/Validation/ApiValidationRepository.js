import { isEmpty } from 'lodash';

import isUndefined from 'lodash/isUndefined';

import { apiValidation } from 'core/infrastructure/api/domain/model/Validation';

import { getCreationDTO } from './Utils/getCreationDTO';
import { parseDataValidationRulesDTO } from './Utils/parseDataValidationRulesDTO';

const create = async (datasetSchemaId, validationRule) => {
  const { expressions } = validationRule;
  const validation = {
    description: validationRule.description,
    automatic: false,
    enabled: validationRule.active ? validationRule.active : false,
    referenceId: validationRule.field.code,
    ruleName: validationRule.name,
    shortCode: validationRule.shortCode,
    type: 'FIELD',
    thenCondition: [validationRule.errorMessage, validationRule.errorLevel.value],
    whenCondition: getCreationDTO(expressions[0], 0, expressions)
  };

  return await apiValidation.create(datasetSchemaId, validation);
};

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

  const validationsData = parseDataValidationRulesDTO(validationsListDTO.rules);
  validationsList.entityTypes = validationsData.entityTypes;
  validationsList.validations = validationsData.validations;
  return validationsList;
};

const update = async (datasetId, validationRule) => {
  const { expressions } = validationRule;
  const validation = {
    ruleId: validationRule.id,
    description: validationRule.description,
    automatic: validationRule.automatic,
    enabled: validationRule.active ? validationRule.active : false,
    referenceId: validationRule.field.code,
    ruleName: validationRule.name,
    shortCode: validationRule.shortCode,
    type: 'FIELD',
    thenCondition: [validationRule.errorMessage, validationRule.errorLevel.value]
  };
  if (!validationRule.automatic) {
    validation.whenCondition = getCreationDTO(expressions[0], 0, expressions);
  }
  return await apiValidation.update(datasetId, validation);
};

export const ApiValidationRepository = {
  create,
  deleteById,
  getAll,
  update
};
