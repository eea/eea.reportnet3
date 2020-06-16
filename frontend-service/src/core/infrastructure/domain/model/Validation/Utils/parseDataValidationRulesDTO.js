import isNil from 'lodash/isNil';

import { Validation } from 'core/domain/model/Validation/Validation';

import { parseExpressionFromDTO } from './parseExpressionFromDTO';
import { parseRowExpressionFromDTO } from './parseRowExpressionFromDTO';
import { parseDatasetRelationFromDTO } from './parseDatasetRelationFromDTO';

export const parseDataValidationRulesDTO = validations => {
  const validationsData = {};
  const entityTypes = [];
  try {
    validationsData.validations = validations.map(validationDTO => {
      let newExpressions = [];
      let newAllExpressions = [];
      let newExpressionsIf = [];
      let newAllExpressionsIf = [];
      let newExpressionsThen = [];
      let newAllExpressionsThen = [];
      let newRelations = {};

      entityTypes.push(validationDTO.type);

      if (validationDTO.type === 'FIELD') {
        const { expressions, allExpressions } = parseExpressionFromDTO(validationDTO.whenCondition);
        newExpressions = expressions;
        newAllExpressions = allExpressions;
      }

      if (validationDTO.type === 'RECORD') {
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

      if (validationDTO.type === 'DATASET') {
        console.log(validationDTO);
        const relations = parseDatasetRelationFromDTO(validationDTO.integrityVO);
        console.log({ relations });
        newRelations = relations;
      }

      return new Validation({
        activationGroup: validationDTO.activationGroup,
        automatic: validationDTO.automatic,
        condition: validationDTO.whenCondition,
        date: validationDTO.activationGroup,
        description: validationDTO.description,
        enabled: validationDTO.enabled,
        entityType: validationDTO.type,
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
        shortCode: validationDTO.shortCode,
        expressions: newExpressions,
        allExpressions: newAllExpressions,
        expressionsIf: newExpressionsIf,
        allExpressionsIf: newAllExpressionsIf,
        expressionsThen: newExpressionsThen,
        allExpressionsThen: newAllExpressionsThen,
        relations: newRelations
      });
    });
  } catch (error) {
    throw new Error('VALIDATION_SERVICE_GET_ALL');
  }
  validationsData.entityTypes = [...new Set(entityTypes)];
  return validationsData;
};
