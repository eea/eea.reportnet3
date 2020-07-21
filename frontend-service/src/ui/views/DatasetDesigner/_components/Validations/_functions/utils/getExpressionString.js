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
      break;
    case 'RECORD':
      if (validation.condition.operator === 'RECORD_IF') {
        return `IF ( ${getComparisonExpressionString(
          validation.expressionsIf,
          tabs
        )} ) THEN ( ${getComparisonExpressionString(validation.expressionsThen, tabs)} )`;
      } else {
        return getComparisonExpressionString(validation.expressions, tabs);
      }
      break;
    default:
      break;
  }
};
