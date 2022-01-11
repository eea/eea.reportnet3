import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import { ValidationRepository } from 'repositories/ValidationRepository';

import { ValidationUtils } from './_utils/ValidationUtils';

export const ValidationService = {
  createFieldRule: async (datasetSchemaId, validationRule) => {
    const { expressions } = validationRule;
    const validation = {
      automatic: false,
      description: validationRule.description,
      enabled: validationRule.active ? validationRule.active : false,
      expressionText: validationRule.expressionText,
      referenceId: validationRule.field.code,
      ruleName: validationRule.name,
      shortCode: validationRule.shortCode,
      sqlSentence: validationRule.sqlSentence,
      thenCondition: [validationRule.errorMessage, validationRule.errorLevel.value],
      type: 'FIELD',
      whenCondition:
        isNil(validationRule.sqlSentence) || isEmpty(validationRule.sqlSentence)
          ? ValidationUtils.getCreationDTO(expressions)
          : null
    };
    return await ValidationRepository.create(datasetSchemaId, validation);
  },

  createRowRule: async (datasetSchemaId, validationRule) => {
    const { expressions, expressionsIf, expressionsThen, expressionType } = validationRule;
    const validation = {
      automatic: false,
      description: validationRule.description,
      enabled: validationRule.active ? validationRule.active : false,
      expressionText: validationRule.expressionText,
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
        params: [
          ValidationUtils.getCreationComparisonDTO(expressionsIf),
          ValidationUtils.getCreationComparisonDTO(expressionsThen)
        ]
      };
    }

    if (expressionType === 'fieldComparison') {
      validation.whenCondition = ValidationUtils.getCreationComparisonDTO(expressions);
    }

    return await ValidationRepository.create(datasetSchemaId, validation);
  },

  createTableRule: async (datasetSchemaId, validationRule) => {
    const validation = {
      automatic: false,
      description: validationRule.description,
      enabled: validationRule.active ? validationRule.active : false,
      expressionText: validationRule.expressionText,
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
    return await ValidationRepository.create(datasetSchemaId, validation);
  },

  delete: async (datasetSchemaId, ruleId) => await ValidationRepository.delete(datasetSchemaId, ruleId),

  downloadQCRulesFile: async (datasetId, fileName) =>
    await ValidationRepository.downloadQCRulesFile(datasetId, fileName),

  downloadShowValidationsFile: async (datasetId, fileName) =>
    await ValidationRepository.downloadShowValidationsFile(datasetId, fileName),

  getAll: async (dataflowId, datasetSchemaId, reporting = false) => {
    const validationsListDTO = await ValidationRepository.getAll(dataflowId, datasetSchemaId);
    if (isUndefined(validationsListDTO.data) || isEmpty(validationsListDTO.data.rules)) {
      return;
    }

    const validationsList = {};
    validationsList.datasetSchemaId = validationsListDTO.data.idDatasetSchema;
    validationsList.rulesSchemaId = validationsListDTO.data.rulesSchemaId;

    if (reporting) {
      validationsListDTO.data.rules = validationsListDTO.data.rules.filter(rule => rule.enabled === true);
    }
    const validationsData = ValidationUtils.parseDataValidationRulesDTO(validationsListDTO.data.rules);
    validationsList.entityTypes = validationsData.entityTypes;
    validationsList.validations = validationsData.validations;

    return validationsList;
  },

  generateQCRulesFile: async datasetId => await ValidationRepository.generateQCRulesFile(datasetId),

  generateShowValidationsFile: async datasetId => await ValidationRepository.generateShowValidationsFile(datasetId),

  runSqlRule: async (datasetId, sqlSentence, showInternalFields) => {
    const { data } = await ValidationRepository.runSqlRule(datasetId, sqlSentence, showInternalFields);

    return ValidationUtils.parseSqlValidation(data);
  },

  updateFieldRule: async (datasetId, validationRule) => {
    const { expressions } = validationRule;
    const validation = {
      automatic: validationRule.automatic,
      description: validationRule.description,
      enabled: validationRule.active ? validationRule.active : false,
      expressionText: validationRule.expressionText,
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
        isNil(validationRule.sqlSentence) || isEmpty(validationRule.sqlSentence)
          ? ValidationUtils.getCreationDTO(expressions)
          : null;
    }
    return await ValidationRepository.update(datasetId, validation);
  },

  updateRowRule: async (datasetId, validationRule) => {
    const { expressions, expressionType, expressionsIf, expressionsThen } = validationRule;
    const validation = {
      automatic: validationRule.automatic,
      description: validationRule.description,
      enabled: validationRule.active ? validationRule.active : false,
      expressionText: validationRule.expressionText,
      referenceId: validationRule.recordSchemaId,
      ruleId: validationRule.id,
      ruleName: validationRule.name,
      shortCode: validationRule.shortCode,
      sqlSentence: validationRule.sqlSentence,
      thenCondition: [validationRule.errorMessage, validationRule.errorLevel.value],
      type: 'RECORD',
      whenCondition: null
    };
    if (!validationRule.automatic) {
      if (expressionType === 'ifThenClause') {
        validation.whenCondition = {
          operator: 'RECORD_IF',
          params: [
            ValidationUtils.getCreationComparisonDTO(expressionsIf),
            ValidationUtils.getCreationComparisonDTO(expressionsThen)
          ]
        };
      }

      if (expressionType === 'fieldComparison') {
        validation.whenCondition = ValidationUtils.getCreationComparisonDTO(expressions);
      }
    }
    return await ValidationRepository.update(datasetId, validation);
  },

  updateTableRule: async (datasetId, validationRule) => {
    const validation = {
      automatic: validationRule.automatic,
      description: validationRule.description,
      enabled: validationRule.active ? validationRule.active : false,
      expressionText: validationRule.expressionText,
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

    return await ValidationRepository.update(datasetId, validation);
  },

  evaluateSqlSentence: async (datasetId, sqlSentence) =>
    await ValidationRepository.evaluateSqlSentence(datasetId, sqlSentence),

  viewUpdated: async datasetId => await ValidationRepository.viewUpdated(datasetId)
};
