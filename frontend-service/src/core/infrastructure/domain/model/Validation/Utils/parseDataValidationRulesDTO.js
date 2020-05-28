import isNil from 'lodash/isNil';

import { Validation } from 'core/domain/model/Validation/Validation';

import { parseExpressionFromDTO } from './parseExpressionFromDTO';

export const parseDataValidationRulesDTO = validations => {
  const validationsData = {};
  const entityTypes = [];
  try {
    validationsData.validations = validations.map(validationDTO => {
      let newExpressions = [];
      let newAllExpressions = [];
      entityTypes.push(validationDTO.type);
      if (validationDTO.type === 'FIELD') {
        const { expressions, allExpressions } = parseExpressionFromDTO(validationDTO.whenCondition);
        newExpressions = expressions;
        newAllExpressions = allExpressions;
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
        allExpressions: newAllExpressions
      });
    });
  } catch (error) {
    throw new Error('VALIDATION_SERVICE_GET_ALL');
  }
  validationsData.entityTypes = [...new Set(entityTypes)];
  return validationsData;
};
