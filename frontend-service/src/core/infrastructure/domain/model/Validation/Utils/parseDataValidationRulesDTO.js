import isNil from 'lodash/isNil';

import { Validation } from 'core/domain/model/Validation/Validation';

import { parseExpressionFromDTO } from './parseExpressionFromDTO';

export const parseDataValidationRulesDTO = validations => {
  const validationsData = {};
  const entityTypes = [];
  try {
    console.log('validations: ', validations);
    validationsData.validations = validations.map(validationDTO => {
      entityTypes.push(validationDTO.type);
      const { expressions, allExpressions } = parseExpressionFromDTO(validationDTO.whenCondition);
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
        expressions,
        allExpressions
      });
    });
  } catch (error) {
    throw new Error('VALIDATION_SERVICE_GET_ALL');
  }
  validationsData.entityTypes = [...new Set(entityTypes)];
  return validationsData;
};
