import isNil from 'lodash/isNil';

import { getComparisonExpressionString } from './getComparisonExpressionString';
import { getFieldExpressionString } from './getFieldExpressionString';

export const getExpressionString = (validation, tabs) => {
  if (validation.automatic) {
    return '';
  }

  switch (validation.entityType) {
    case 'FIELD':
      const field = {
        label: validation.fieldName,
        code: validation.id
      };

      return getFieldExpressionString(validation.expressions, field);

    case 'RECORD':
      if (!isNil(validation?.condition?.operator) && validation.condition.operator === 'RECORD_IF') {
        return `IF ( ${getComparisonExpressionString(
          validation.expressionsIf,
          tabs
        )} ) THEN ( ${getComparisonExpressionString(validation.expressionsThen, tabs)} )`;
      } else if (!isNil(validation.sqlSentence)) {
        return validation.sqlSentence;
      } else {
        return getComparisonExpressionString(validation.expressions, tabs);
      }

    default:
      return '';     
  }
};
