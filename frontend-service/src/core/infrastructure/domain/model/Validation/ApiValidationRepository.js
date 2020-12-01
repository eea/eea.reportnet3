import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import { apiValidation } from 'core/infrastructure/api/domain/model/Validation';

import { getCreationComparisonDTO } from './Utils/getCreationComparisonDTO';
import { getCreationDTO } from './Utils/getCreationDTO';
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
    sqlSentence: validationRule.sqlSentence,
    thenCondition: [validationRule.errorMessage, validationRule.errorLevel.value],
    type: 'FIELD',
    whenCondition:
      isNil(validationRule.sqlSentence) || isEmpty(validationRule.sqlSentence) ? getCreationDTO(expressions) : null
  };
  return await apiValidation.create(datasetSchemaId, validation);
};

const createTableRule = async (datasetSchemaId, validationRule) => {
  const validation = {
    automatic: false,
    description: validationRule.description,
    enabled: validationRule.active ? validationRule.active : false,
    integrityVO:
      isNil(validationRule.sqlSentence) || isEmpty(validationRule.sqlSentence)
        ? {
            isDoubleReferenced: validationRule.relations.isDoubleReferenced,
            originDatasetSchemaId: validationRule.relations.originDatasetSchema,
            originFields: validationRule.relations.links.map(link => link.originField.code),
            referencedDatasetSchemaId: validationRule.relations.referencedDatasetSchema.code,
            referencedFields: validationRule.relations.links.map(link => link.referencedField.code)
          }
        : null,
    referenceId: validationRule.table.code,
    ruleName: validationRule.name,
    shortCode: validationRule.shortCode,
    sqlSentence: validationRule.sqlSentence,
    thenCondition: [validationRule.errorMessage, validationRule.errorLevel.value],
    type: 'TABLE',
    whenCondition: null
  };
  return await apiValidation.create(datasetSchemaId, validation);
};

const createRowRule = async (datasetSchemaId, validationRule) => {
  const { expressions, expressionsIf, expressionsThen, expressionType } = validationRule;
  const validation = {
    automatic: false,
    description: validationRule.description,
    enabled: validationRule.active ? validationRule.active : false,
    referenceId: validationRule.recordSchemaId,
    ruleName: validationRule.name,
    shortCode: validationRule.shortCode,
    sqlSentence: validationRule.sqlSentence,
    thenCondition: [validationRule.errorMessage, validationRule.errorLevel.value],
    type: 'RECORD',
    whenCondition: null
  };

  if (expressionType === 'ifThenClause') {
    validation.whenCondition = {
      operator: 'RECORD_IF',
      params: [getCreationComparisonDTO(expressionsIf), getCreationComparisonDTO(expressionsThen)]
    };
  }

  if (expressionType === 'fieldComparison') {
    validation.whenCondition = getCreationComparisonDTO(expressions);
  }

  return await apiValidation.create(datasetSchemaId, validation);
};

const deleteById = async (datasetSchemaId, ruleId) => {
  return await apiValidation.deleteById(datasetSchemaId, ruleId);
};

const getAll = async (datasetSchemaId, reporting = false) => {
  const validationsListDTO = await apiValidation.getAll(datasetSchemaId);
  if (isUndefined(validationsListDTO) || isEmpty(validationsListDTO.rules)) {
    return;
  }

  const validationsList = {};
  validationsList.datasetSchemaId = validationsListDTO.idDatasetSchema;
  validationsList.rulesSchemaId = validationsListDTO.rulesSchemaId;

  if (reporting) {
    validationsListDTO.rules = validationsListDTO.rules.filter(rule => rule.enabled === true);
  }
  const validationsData = parseDataValidationRulesDTO(validationsListDTO.rules);
  validationsList.entityTypes = validationsData.entityTypes;
  validationsList.validations = validationsData.validations;

  return validationsList;
};

const update = async (datasetId, validationRule) => {
  const { expressions } = validationRule;
  const validation = {
    automatic: validationRule.automatic,
    description: validationRule.description,
    enabled: validationRule.active ? validationRule.active : false,
    referenceId: validationRule.field.code,
    ruleId: validationRule.id,
    ruleName: validationRule.name,
    shortCode: validationRule.shortCode,
    sqlSentence: validationRule.sqlSentence,
    thenCondition: [validationRule.errorMessage, validationRule.errorLevel.value],
    type: 'FIELD'
  };
  if (!validationRule.automatic) {
    validation.whenCondition =
      isNil(validationRule.sqlSentence) || isEmpty(validationRule.sqlSentence) ? getCreationDTO(expressions) : null;
  }
  return await apiValidation.update(datasetId, validation);
};

const updateRowRule = async (datasetId, validationRule) => {
  const { expressions, expressionType, expressionsIf, expressionsThen } = validationRule;
  const validation = {
    automatic: validationRule.automatic,
    description: validationRule.description,
    enabled: validationRule.active ? validationRule.active : false,
    referenceId: validationRule.recordSchemaId,
    ruleId: validationRule.id,
    ruleName: validationRule.name,
    shortCode: validationRule.shortCode,
    sqlSentence: validationRule.sqlSentence,
    thenCondition: [validationRule.errorMessage, validationRule.errorLevel.value],
    type: validationRule.ruleType,
    whenCondition: null
  };
  if (!validationRule.automatic) {
    if (expressionType === 'ifThenClause') {
      validation.whenCondition = {
        operator: 'RECORD_IF',
        params: [getCreationComparisonDTO(expressionsIf), getCreationComparisonDTO(expressionsThen)]
      };
    }

    if (expressionType === 'fieldComparison') {
      validation.whenCondition = getCreationComparisonDTO(expressions);
    }
  }
  return await apiValidation.update(datasetId, validation);
};

const updateDatasetRule = async (datasetId, validationRule) => {
  const validation = {
    automatic: validationRule.automatic,
    description: validationRule.description,
    enabled: validationRule.active ? validationRule.active : false,
    referenceId: validationRule.table.code,
    ruleId: validationRule.id,
    ruleName: validationRule.name,
    shortCode: validationRule.shortCode,
    sqlSentence: validationRule.sqlSentence,
    thenCondition: [validationRule.errorMessage, validationRule.errorLevel.value],
    type: 'TABLE',
    whenCondition: null
  };

  if (!validationRule.automatic) {
    validation.integrityVO =
      isNil(validationRule.sqlSentence) || isEmpty(validationRule.sqlSentence)
        ? {
            id: validationRule.relations.id,
            isDoubleReferenced: validationRule.relations.isDoubleReferenced,
            originDatasetSchemaId: validationRule.relations.originDatasetSchema,
            originFields: validationRule.relations.links.map(link => link.originField.code),
            referencedDatasetSchemaId: validationRule.relations.referencedDatasetSchema.code,
            referencedFields: validationRule.relations.links.map(link => link.referencedField.code)
          }
        : null;
  }

  return await apiValidation.update(datasetId, validation);
};

export const ApiValidationRepository = {
  create,
  createTableRule,
  createRowRule,
  deleteById,
  getAll,
  update,
  updateDatasetRule,
  updateRowRule
};
