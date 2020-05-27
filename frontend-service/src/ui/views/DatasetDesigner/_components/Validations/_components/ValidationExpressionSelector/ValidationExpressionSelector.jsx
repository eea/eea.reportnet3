import React from 'react';

import { ComparisonExpression } from 'ui/views/DatasetDesigner/_components/Validations/_components/ComparisonExpression';
import { ValidationExpression } from 'ui/views/DatasetDesigner/_components/Validations/_components/ValidationExpression';
import { ValidationExpressionGroup } from 'ui/views/DatasetDesigner/_components/Validations/_components/ValidationExpressionGroup';

export const ValidationExpressionSelector = props => {
  const { expressionType } = props;
  if (props.expressionValues.expressions.length > 0) {
    return <ValidationExpressionGroup {...props} />;
  }
  if (expressionType === 'fieldComparison') {
    return <ComparisonExpression {...props} />;
  }
  if (expressionType === 'ifThenClause') {
    return <ComparisonExpression {...props} />;
  }
  return <ValidationExpression {...props} />;
};
