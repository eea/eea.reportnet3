import isEmpty from 'lodash/isEmpty';

import isUndefined from 'lodash/isUndefined';

import { apiValidation } from 'core/infrastructure/api/domain/model/Validation';

import { getCreationDTO } from './Utils/getCreationDTO';
import { getCreationComparisonDTO } from './Utils/getCreationComparisonDTO';
import { parseDataValidationRulesDTO } from './Utils/parseDataValidationRulesDTO';

const create = async (datasetSchemaId, validationRule) => {
  const { expressions } = validationRule;
  const validation = {
    automatic: false,
    description: validationRule.description,
    enabled: validationRule.active ? validationRule.active : false,
    referenceId: validationRule.field.code,
    ruleName: validationRule.name,
    shortCode: validationRule.shortCode,
    thenCondition: [validationRule.errorMessage, validationRule.errorLevel.value],
    type: 'FIELD',
    whenCondition: getCreationDTO(expressions)
  };
  return await apiValidation.create(datasetSchemaId, validation);
};

const createDatasetRule = async (datasetSchemaId, validationRule) => {
  const validation = {
    automatic: false,
    description: validationRule.description,
    enabled: validationRule.active ? validationRule.active : false,
    referenceId: validationRule.recordSchemaId,
    ruleName: validationRule.name,
    shortCode: validationRule.shortCode,
    integrityVO: {
      isDoubleReferenced: validationRule.isDoubleReferenced,
      originDatasetId: validationRule.relations.originDatasetSchema,
      originFields: validationRule.relations.links.map(link => link.originField.code),
      referencedDatasetId: validationRule.relations.referencedDatasetSchema.code,
      referencedFields: validationRule.relations.links.map(link => link.referencedField.code)
    },
    thenCondition: [validationRule.errorMessage, validationRule.errorLevel.value],
    type: 'DATASET',
    whenCondition: null
  };
  return await apiValidation.create(datasetSchemaId, validation);
};

const createRowRule = async (datasetSchemaId, validationRule) => {
  const { expressions } = validationRule;
  const validation = {
    automatic: false,
    description: validationRule.description,
    enabled: validationRule.active ? validationRule.active : false,
    referenceId: validationRule.recordSchemaId,
    ruleName: validationRule.name,
    shortCode: validationRule.shortCode,
    thenCondition: [validationRule.errorMessage, validationRule.errorLevel.value],
    type: 'RECORD',
    whenCondition: getCreationComparisonDTO(expressions)
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
    validation.whenCondition = getCreationDTO(expressions);
  }
  return await apiValidation.update(datasetId, validation);
};

const updateRowRule = async (datasetId, validationRule) => {
  const { expressions } = validationRule;
  const validation = {
    ruleId: validationRule.id,
    description: validationRule.description,
    automatic: validationRule.automatic,
    enabled: validationRule.active ? validationRule.active : false,
    referenceId: validationRule.recordSchemaId,
    ruleName: validationRule.name,
    shortCode: validationRule.shortCode,
    type: 'RECORD',
    thenCondition: [validationRule.errorMessage, validationRule.errorLevel.value]
  };
  if (!validationRule.automatic) {
    validation.whenCondition = getCreationComparisonDTO(expressions);
  }
  return await apiValidation.update(datasetId, validation);
};

export const ApiValidationRepository = {
  create,
  createDatasetRule,
  createRowRule,
  deleteById,
  getAll,
  update,
  updateRowRule
};
