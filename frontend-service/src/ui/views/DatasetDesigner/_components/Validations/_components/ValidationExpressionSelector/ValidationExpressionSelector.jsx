import React from 'react';

import { ValidationExpression } from 'ui/views/DatasetDesigner/_components/Validations/_components/ValidationExpression';
import { ValidationExpressionGroup } from 'ui/views/DatasetDesigner/_components/Validations/_components/ValidationExpressionGroup';

export const ValidationExpressionSelector = props => {
  if (props.expressionValues.expressions.length > 0) {
    return <ValidationExpressionGroup {...props} />;
  }
  return <ValidationExpression {...props} />;
};
