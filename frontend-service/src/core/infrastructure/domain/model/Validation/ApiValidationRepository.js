import { config } from 'conf';

import { isEmpty } from 'lodash';

import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { apiValidation } from 'core/infrastructure/api/domain/model/Validation';
import { Validation } from 'core/domain/model/Validation/Validation';

const buildExpression = expression => {
  if (expression.operatorType == 'LEN') {
    return {
      operator: config.validations.operatorEquivalences[expression.operatorValue],
      arg1: parseInt(expression.expressionValue),
      arg2: {
        operator: 'LEN',
        arg1: 'VALUE'
      }
    };
  }
  return {
    arg1: 'VALUE',
    operator: config.validations.operatorEquivalences[expression.operatorValue],
    arg2: !config.validations.nonNumericOperators.includes(expression.operatorType)
      ? parseInt(expression.expressionValue)
      : expression.expressionValue
  };
};
const buildNode = (expression, index, expressions) => {
  return {
    arg1: buildTransferDTO(expression, 0, []),
    operator: expressions[index + 1].union,
    arg2:
      index + 1 < expressions.length - 1
        ? buildTransferDTO(expressions[index + 1], index + 1, expressions)
        : buildTransferDTO(expressions[index + 1], 0, [])
  };
};

const buildTransferDTO = (expression, index, expressions) => {
  if (expressions.length > 1) {
    return buildNode(expression, index, expressions);
  }
  if (expression.expressions.length > 1) {
    return buildNode(expression.expressions[0], 0, expression.expressions);
  }
  return buildExpression(expression);
};

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
    whenCondition: buildTransferDTO(expressions[0], 0, expressions)
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

const parseDataValidationRulesDTO = validations => {
  const validationsData = {};
  const entityTypes = [];

  validationsData.validations = validations.map(validationDTO => {
    entityTypes.push(validationDTO.type);
    return new Validation({
      activationGroup: validationDTO.activationGroup,
      automatic: validationDTO.automatic,
      condition: validationDTO.whenCondition,
      date: validationDTO.activationGroup,
      description: isNil(validationDTO.description) ? validationDTO.description : '',
      enabled: isNil(validationDTO.enabled) ? validationDTO.enabled : '',
      entityType: isNil(validationDTO.type) ? validationDTO.type : '',
      id: isNil(validationDTO.ruleId) ? validationDTO.ruleId : '',
      levelError:
        !isNil(validationDTO.thenCondition) && !isNil(validationDTO.thenCondition[1])
          ? validationDTO.thenCondition[1]
          : '',
      message:
        !isNil(validationDTO.thenCondition) && !isNil(validationDTO.thenCondition[0])
          ? validationDTO.thenCondition[0]
          : '',
      name: !isNil(validationDTO.ruleName) ? validationDTO.ruleName : '',
      referenceId: !isNil(validationDTO.referenceId) ? validationDTO.referenceId : '',
      shortCode: !isNil(validationDTO.shortCode) ? validationDTO.shortCode : ''
    });
  });

  validationsData.entityTypes = [...new Set(entityTypes)];
  return validationsData;
};

const translateRules = rule => {
  console.log('*'.repeat(60));
  console.log('translateRules', rule);
  console.log('*'.repeat(60));
  return rule;
};

const validationToTransfer = validation => {
  const whenCondition = translateRules(validation.rules);

  return {
    description: validation.description,
    ensabled: validation.active,
    referenceId: validation.fieldId,
    ruleName: '',
    shortCode: validation.shortCode,
    type: 'FIELD',
    thenCondition: [validation.errorMessage, validation.errorLevel],
    whenCondition: {
      whenCondition
    }
  };
};
export const ApiValidationRepository = {
  create,
  deleteById,
  getAll
};
