import React from 'react';

import { ValidationExpresion } from 'ui/views/DatasetDesigner/_components/CreateValidation/_components/ValidationExpresion';
import { ValidationExpresionGroup } from 'ui/views/DatasetDesigner/_components/CreateValidation/_components/ValidationExpresionGroup';

export const ValidationExpressionSelector = props => {
  if (props.expresionValues.expresions.length > 0) {
    return <ValidationExpresionGroup {...props} />;
  }
  return <ValidationExpresion {...props} />;
};
