import customParseFormat from 'dayjs/plugin/customParseFormat';
import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isObject from 'lodash/isObject';
import uniqueId from 'lodash/uniqueId';

import { Validation } from 'entities/Validation';

import { config } from 'conf';

dayjs.extend(customParseFormat);

const getOperatorEquivalence = (valueTypeSelector, operatorType, operatorValue = null) => {
  const {
    validations: { comparisonOperatorEquivalences, comparisonValuesOperatorEquivalences }
  } = config;

  if (isNil(operatorValue)) {
    if (valueTypeSelector === 'value') return comparisonValuesOperatorEquivalences[operatorType].type;
    return comparisonOperatorEquivalences[operatorType].type;
  } else {
    if (comparisonOperatorEquivalences[operatorType]) {
      if (valueTypeSelector === 'value') return comparisonValuesOperatorEquivalences[operatorType][operatorValue];
      return comparisonOperatorEquivalences[operatorType][operatorValue];
    }
  }
};

const getOperatorEquivalenceExpression = (operatorType, operatorValue = null) => {
  const {
    validations: { operatorEquivalences }
  } = config;

  if (isNil(operatorValue)) {
    return operatorEquivalences[operatorType].type;
  } else {
    if (operatorEquivalences[operatorType]) {
      return operatorEquivalences[operatorType][operatorValue];
    }
  }
};

const getComparisonExpression = expression => {
  const { operatorType, operatorValue, field1, field2, field1Type, valueTypeSelector, field2Type } = expression;
  let transField2 = field2;

  if (field1Type === 'NUMBER_DECIMAL' && valueTypeSelector === 'value') {
    transField2 = Number.parseFloat(field2);
  }
  if (field1Type === 'NUMBER_INTEGER' && valueTypeSelector === 'value') {
    transField2 = Number(field2);
  }

  if (expression.expressions.length > 1) {
    return getCreationComparisonDTO(expression.expressions);
  } else {
    const dateNumberOperators = ['year', 'month', 'day', 'yearDateTime', 'monthDateTime', 'dayDateTime'];
    if (dateNumberOperators.includes(operatorType) && field2Type === 'NUMBER_INTEGER') {
      return {
        operator: getOperatorEquivalence(valueTypeSelector, `${operatorType}Number`, operatorValue),
        params: [field1, transField2]
      };
    }

    if (operatorType === 'date' && valueTypeSelector === 'value') {
      return {
        operator: getOperatorEquivalence(valueTypeSelector, operatorType, operatorValue),
        params: [field1, dayjs(transField2).format('YYYY-MM-DD')]
      };
    }
    if (operatorType === 'dateTime' && valueTypeSelector === 'value') {
      return {
        operator: getOperatorEquivalence(valueTypeSelector, operatorType, operatorValue),
        params: [field1, dayjs(transField2).format('YYYY-MM-DD HH:mm:ss')]
      };
    }

    if (operatorType === 'LEN' && valueTypeSelector === 'value') {
      return {
        operator: getOperatorEquivalence(valueTypeSelector, operatorType, operatorValue),
        params: [field1, Number(transField2)]
      };
    }

    if (operatorValue === 'IS NULL' || operatorValue === 'IS NOT NULL') {
      return {
        operator: getOperatorEquivalence(valueTypeSelector, operatorType, operatorValue),
        params: [field1]
      };
    }
    return {
      operator: getOperatorEquivalence(valueTypeSelector, operatorType, operatorValue),
      params: [field1, transField2]
    };
  }
};

const getCreationComparisonDTO = expressions => {
  if (!isEmpty(expressions)) {
    if (expressions.length > 1) {
      const params = [];
      let operator = '';

      expressions.forEach((expression, index) => {
        if (index === 0) {
          operator = expressions[index + 1].union;
          if (expression.expressions.length > 0) {
            params.push(getCreationComparisonDTO(expression.expressions));
          } else {
            params.push(getComparisonExpression(expression));
          }

          if (!isNil(expressions[index + 2])) {
            const nextExpressions = expressions.slice(index + 1);
            params.push(getCreationComparisonDTO(nextExpressions));
          } else {
            if (expressions[index + 1].expressions.length > 0) {
              params.push(getCreationComparisonDTO(expressions[index + 1].expressions));
            } else {
              params.push(getComparisonExpression(expressions[index + 1]));
            }
          }
        }
      });

      return {
        operator: config.validations.comparisonOperatorEquivalences.logicalOperators[operator],
        params
      };
    }
    const [expression] = expressions;
    return getComparisonExpression(expression);
  }
};

const getCreationDTO = expressions => {
  if (!isEmpty(expressions)) {
    if (expressions.length > 1) {
      const params = [];
      let operator = '';

      expressions.forEach((expression, index) => {
        if (index === 0) {
          operator = expressions[index + 1].union;

          if (expression.expressions.length > 0) {
            params.push(getCreationDTO(expression.expressions));
          } else {
            params.push(getExpression(expression));
          }

          if (!isNil(expressions[index + 2])) {
            const nextExpressions = expressions.slice(index + 1);
            params.push(getCreationDTO(nextExpressions));
          } else {
            if (expressions[index + 1].expressions.length > 0) {
              params.push(getCreationDTO(expressions[index + 1].expressions));
            } else {
              params.push(getExpression(expressions[index + 1]));
            }
          }
        }
      });

      return {
        operator: config.validations.operatorEquivalences.logicalOperators[operator],
        params
      };
    }
  }
  const [expression] = expressions;
  return getExpression(expression);
};

const getExpression = expression => {
  const { operatorType, operatorValue, expressionValue } = expression;
  const {
    validations: { nonNumericOperators }
  } = config;

  const operatorEquivalence = getOperatorEquivalenceExpression(operatorType, operatorValue);

  if (expression.expressions.length > 1) {
    return getCreationDTO(expression.expressions);
  } else {
    if (operatorValue === 'IS NULL' || operatorValue === 'IS NOT NULL') {
      return { operator: operatorEquivalence, params: ['VALUE'] };
    }

    if (operatorType === 'LEN') {
      return {
        operator: operatorEquivalence,
        params: [
          {
            operator: getOperatorEquivalenceExpression(operatorType),
            params: ['VALUE']
          },
          Number(expressionValue)
        ]
      };
    }

    if (operatorType === 'date') {
      return {
        operator: operatorEquivalence,
        params: ['VALUE', dayjs(expressionValue).format('YYYY-MM-DD')]
      };
    }

    if (operatorType === 'dateTime') {
      return {
        operator: operatorEquivalence,
        params: ['VALUE', dayjs(expressionValue).format('YYYY-MM-DD HH:mm:ss')]
      };
    }

    if (operatorEquivalence === 'FIELD_NUM_MATCH') {
      return {
        operator: operatorEquivalence,
        params: ['VALUE', expressionValue]
      };
    }

    return {
      operator: operatorEquivalence,
      params: ['VALUE', !nonNumericOperators.includes(operatorType) ? Number(expressionValue) : expressionValue]
    };
  }
};

const getExpressionFromDTO = (expression, allExpressions, parentUnion) => {
  const union = !isNil(parentUnion) ? config.validations.reverseEquivalences[parentUnion] : '';
  const newExpression = {};
  newExpression.expressionId = uniqueId();
  newExpression.group = false;
  newExpression.union = union;
  newExpression.operatorValue = config.validations.reverseEquivalences[expression.operator];
  newExpression.expressionValue = expression.params[1];
  newExpression.expressions = [];
  newExpression.operatorType = getExpressionOperatorType(expression.operator);

  if (isObject(expression.params[0])) {
    newExpression.operatorType = getExpressionOperatorType(expression.params[0].operator);
    newExpression.expressionValue = expression.params[1];
  }

  if (newExpression.operatorType === 'date' || newExpression.operatorType === 'dateTime') {
    newExpression.expressionValue =
      newExpression.operatorType === 'date'
        ? new Date(expression.params[1])
        : dayjs(expression.params[1], 'YYYY-MM-DDTHH:mm:ss[Z]');
  }

  allExpressions.push(newExpression);
  return newExpression;
};

const getExpressionOperatorType = (operator, type = 'field') => {
  const { validations } = config;
  const confOperators = validations[`${type}OperatorsTypesFromDTO`];

  const selectedOperators = confOperators.filter(operatorTypeObject => {
    return operatorTypeObject.operators.includes(operator) && operatorTypeObject;
  });

  const [selectedOperator] = selectedOperators;
  return selectedOperator.operatorType;
};

const getGroupFromDTO = (expression, allExpressions, parentOperator) => {
  const {
    validations: { reverseEquivalences }
  } = config;
  const union = !isNil(parentOperator) ? reverseEquivalences[parentOperator] : '';

  const newExpression = {};
  newExpression.expressionId = uniqueId;
  newExpression.group = false;
  newExpression.union = union;
  newExpression.operator = '';
  newExpression.operatorValue = '';
  newExpression.expressionValue = '';
  const subExpressions = [];

  selectorFromDTO({ operator: expression.operator, params: expression.params }, subExpressions, allExpressions);
  newExpression.expressions = subExpressions;
  allExpressions.push(newExpression);
  return newExpression;
};

const selectorFromDTO = (expression, expressions, allExpressions, parentOperator = null) => {
  const {
    validations: { logicalOperatorFromDTO }
  } = config;
  const [firstParam, secondParam] = expression.params;
  const { operator } = expression;

  if (logicalOperatorFromDTO.includes(operator)) {
    if (logicalOperatorFromDTO.includes(firstParam.operator)) {
      expressions.push(getGroupFromDTO(firstParam, allExpressions, parentOperator));
    } else {
      expressions.push(getExpressionFromDTO(firstParam, allExpressions, parentOperator));
    }

    if (logicalOperatorFromDTO.includes(secondParam.operator)) {
      selectorFromDTO(secondParam, expressions, allExpressions, operator);
    } else {
      expressions.push(getExpressionFromDTO(secondParam, allExpressions, operator));
    }
  } else {
    expressions.push(getExpressionFromDTO(expression, allExpressions, null));
  }
};

const getValueTypeSelector = operator => {
  const operatorsChunks = operator.split('_');
  const [thirdToLastChunk, secondToLastChunk, lastChunk] = operatorsChunks.slice(-3);
  if (
    lastChunk === 'RECORD' ||
    secondToLastChunk === 'RECORD' ||
    (thirdToLastChunk === 'RECORD' && operatorsChunks.length > 3)
  ) {
    return 'field';
  }
  return 'value';
};

const getRowExpressionFromDTO = (expression, allExpressions, parentUnion) => {
  const union = !isNil(parentUnion) ? config.validations.reverseEquivalences[parentUnion] : '';
  const newExpression = {};
  newExpression.expressionId = uniqueId();
  newExpression.group = false;
  newExpression.union = union;
  newExpression.operatorValue = config.validations.reverseEquivalences[expression.operator];
  newExpression.field1 = expression.params[0];
  newExpression.expressions = [];
  newExpression.valueTypeSelector = getValueTypeSelector(expression.operator);

  if (isObject(expression.params[1])) {
    newExpression.operatorType = getExpressionOperatorType(expression.params[1].operator, 'row');
    if (
      (newExpression.operatorType === 'date' || newExpression.operatorType === 'dateTime') &&
      newExpression.valueTypeSelector === 'value'
    ) {
      newExpression.field2 =
        newExpression.operatorType === 'date'
          ? new Date(expression.params[1].params[0])
          : dayjs(expression.params[1].params[0], 'YYYY-MM-DDTHH:mm:ss[Z]');
    } else {
      newExpression.field2 = expression.params[1].params[0];
    }
  } else {
    newExpression.operatorType = getExpressionOperatorType(expression.operator, 'row');
    if (
      (newExpression.operatorType === 'date' || newExpression.operatorType === 'dateTime') &&
      newExpression.valueTypeSelector === 'value'
    ) {
      newExpression.field2 =
        newExpression.operatorType === 'date'
          ? new Date(expression.params[1])
          : dayjs(expression.params[1], 'YYYY-MM-DDTHH:mm:ss[Z]');
    } else {
      newExpression.field2 = expression.params[1];
    }
  }
  allExpressions.push(newExpression);

  return newExpression;
};

const getRowGroupFromDTO = (expression, allExpressions, parentOperator) => {
  const {
    validations: { reverseEquivalences }
  } = config;
  const union = !isNil(parentOperator) ? reverseEquivalences[parentOperator] : '';

  const newExpression = {};
  newExpression.expressionId = uniqueId();
  newExpression.group = false;
  newExpression.union = union;
  newExpression.operator = '';
  newExpression.operatorValue = '';
  newExpression.expressionValue = '';
  const subExpressions = [];

  selectorRowFromDTO({ operator: expression.operator, params: expression.params }, subExpressions, allExpressions);
  newExpression.expressions = subExpressions;
  allExpressions.push(newExpression);

  return newExpression;
};

const selectorRowFromDTO = (expression, expressions, allExpressions, parentOperator = null) => {
  const {
    validations: { logicalRowOperatorFromDTO }
  } = config;
  const [firstParam, secondParam] = expression.params;
  const { operator } = expression;

  if (logicalRowOperatorFromDTO.includes(operator)) {
    if (logicalRowOperatorFromDTO.includes(firstParam.operator)) {
      expressions.push(getRowGroupFromDTO(firstParam, allExpressions, parentOperator));
    } else {
      expressions.push(getRowExpressionFromDTO(firstParam, allExpressions, parentOperator));
    }

    if (logicalRowOperatorFromDTO.includes(secondParam.operator)) {
      selectorRowFromDTO(secondParam, expressions, allExpressions, operator);
    } else {
      expressions.push(getRowExpressionFromDTO(secondParam, allExpressions, operator));
    }
  } else {
    expressions.push(getRowExpressionFromDTO(expression, allExpressions, null));
  }
};

const parseDatasetRelationFromDTO = integrityVO => {
  if (!isNil(integrityVO)) {
    const relations = {
      id: integrityVO.id,
      isDoubleReferenced: !isNil(integrityVO.isDoubleReferenced) ? integrityVO.isDoubleReferenced : false,
      originDatasetSchema: integrityVO.originDatasetSchemaId,
      referencedDatasetSchema: { code: integrityVO.referencedDatasetSchemaId, label: '' },
      links: integrityVO.originFields.map((originField, i) => {
        return {
          linkId: uniqueId(),
          originField: { code: originField, label: '' },
          referencedField: { code: integrityVO.referencedFields[i], label: '' }
        };
      })
    };
    return relations;
  }
  return {};
};

const parseDataValidationRulesDTO = validations => {
  const validationsData = {};
  const entityTypes = [];

  validationsData.validations = validations.map(validationDTO => {
    let newAllExpressions = [];
    let newAllExpressionsIf = [];
    let newAllExpressionsThen = [];
    let newExpressions = [];
    let newExpressionsIf = [];
    let newExpressionsThen = [];
    let newRelations = {};
    entityTypes.push(validationDTO.type);

    if ((isNil(validationDTO.sqlSentence) || validationDTO.sqlSentence === '') && validationDTO.type === 'FIELD') {
      const { expressions, allExpressions } = parseExpressionFromDTO(validationDTO.whenCondition);
      newExpressions = expressions;
      newAllExpressions = allExpressions;
    }

    if (validationDTO.type === 'RECORD') {
      if (isNil(validationDTO.sqlSentence) || validationDTO.sqlSentence === '') {
        if (validationDTO.whenCondition.operator === 'RECORD_IF') {
          const { expressions: expressionsIf, allExpressions: allExpressionsIf } = parseRowExpressionFromDTO(
            validationDTO.whenCondition.params[0]
          );
          const { expressions: expressionsThen, allExpressions: allExpressionsThen } = parseRowExpressionFromDTO(
            validationDTO.whenCondition.params[1]
          );
          newExpressionsIf = expressionsIf;
          newAllExpressionsIf = allExpressionsIf;
          newExpressionsThen = expressionsThen;
          newAllExpressionsThen = allExpressionsThen;
        } else {
          const { expressions, allExpressions } = parseRowExpressionFromDTO(validationDTO.whenCondition);
          newExpressions = expressions;
          newAllExpressions = allExpressions;
        }
      }
    }

    if ((isNil(validationDTO.sqlSentence) || validationDTO.sqlSentence === '') && validationDTO.type === 'TABLE') {
      const relations = parseDatasetRelationFromDTO(validationDTO.integrityVO);
      newRelations = relations;
    }

    return new Validation({
      activationGroup: validationDTO.activationGroup,
      allExpressions: newAllExpressions,
      allExpressionsIf: newAllExpressionsIf,
      allExpressionsThen: newAllExpressionsThen,
      automatic: validationDTO.automatic,
      automaticType: validationDTO.automaticType,
      condition: validationDTO.whenCondition,
      date: validationDTO.activationGroup,
      description: validationDTO.description,
      enabled: validationDTO.enabled,
      entityType: validationDTO.type,
      expressionText: validationDTO.expressionText,
      expressions: newExpressions,
      expressionsIf: newExpressionsIf,
      expressionsThen: newExpressionsThen,
      id: validationDTO.ruleId,
      isCorrect: validationDTO.verified,
      levelError:
        !isNil(validationDTO.thenCondition) && !isNil(validationDTO.thenCondition[1])
          ? validationDTO.thenCondition[1]
          : '',
      message:
        !isNil(validationDTO.thenCondition) && !isNil(validationDTO.thenCondition[0])
          ? validationDTO.thenCondition[0]
          : '',
      name: validationDTO.ruleName,
      referenceId: validationDTO.referenceId,
      relations: newRelations,
      shortCode: validationDTO.shortCode,
      sqlError: validationDTO.sqlError,
      sqlSentence: validationDTO.sqlSentence === '' ? null : validationDTO.sqlSentence
    });
  });

  validationsData.entityTypes = [...new Set(entityTypes)];
  return validationsData;
};

const parseExpressionFromDTO = expression => {
  const expressions = [];
  const allExpressions = [];

  if (!isNil(expression)) {
    selectorFromDTO(expression, expressions, allExpressions);
  }

  return {
    expressions,
    allExpressions
  };
};

const parseRowExpressionFromDTO = expression => {
  const expressions = [];
  const allExpressions = [];
  if (!isNil(expression)) {
    selectorRowFromDTO(expression, expressions, allExpressions);
  }
  return {
    expressions,
    allExpressions
  };
};

const createValidation = (entityType, id, levelError, message) =>
  new Validation({ date: new Date(Date.now()).toString(), entityType, id, levelError, message });

export const ValidationUtils = {
  createValidation,
  getComparisonExpression,
  getCreationComparisonDTO,
  getCreationDTO,
  getExpression,
  getExpressionFromDTO,
  getExpressionOperatorType,
  getGroupFromDTO,
  getRowExpressionFromDTO,
  getRowGroupFromDTO,
  parseDatasetRelationFromDTO,
  parseDataValidationRulesDTO,
  parseExpressionFromDTO,
  parseRowExpressionFromDTO
};
