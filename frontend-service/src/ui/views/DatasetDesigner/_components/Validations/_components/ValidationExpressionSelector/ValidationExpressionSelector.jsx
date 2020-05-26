import React from 'react';

import { ComparisonExpression } from 'ui/views/DatasetDesigner/_components/Validations/_components/ComparisonExpression';
import { ValidationExpression } from 'ui/views/DatasetDesigner/_components/Validations/_components/ValidationExpression';
import { ValidationExpressionGroup } from 'ui/views/DatasetDesigner/_components/Validations/_components/ValidationExpressionGroup';

export const ValidationExpressionSelector = props => {
  console.log('props.expressionType', props.expressionType);
  if (props.expressionValues.expressions.length > 0) {
    return <ValidationExpressionGroup {...props} />;
  }
  if (props.expressionType === 'fieldComparison') {
    console.log('kk');
    return <ComparisonExpression {...props} />;
  }
  if (props.expressionType === 'ifThenClause') {
    return <ComparisonExpression {...props} />;
  }
  return <ValidationExpression {...props} />;
};
