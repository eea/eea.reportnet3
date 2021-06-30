import isNil from 'lodash/isNil';

import { Validation } from 'core/domain/model/Validation/Validation';

import { parseDatasetRelationFromDTO } from './parseDatasetRelationFromDTO';
import { parseExpressionFromDTO } from './parseExpressionFromDTO';
import { parseRowExpressionFromDTO } from './parseRowExpressionFromDTO';

export const parseDataValidationRulesDTO = validations => {
  const validationsData = {};
  const entityTypes = [];
  try {
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
        sqlSentence: validationDTO.sqlSentence === '' ? null : validationDTO.sqlSentence
      });
    });
  } catch (error) {
    throw new Error('VALIDATION_SERVICE_GET_ALL');
  }
  validationsData.entityTypes = [...new Set(entityTypes)];
  return validationsData;
};
