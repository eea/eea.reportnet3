import React from 'react';

import { ValidationExpresion } from 'ui/views/DatasetDesigner/_components/CreateValidation/_components/ValidationExpresion';
import { ValidationExpresionGroup } from 'ui/views/DatasetDesigner/_components/CreateValidation/_components/ValidationExpresionGroup';

export const ValidationExpressionSelector = props => {
  console.info('#'.repeat(60));
  console.info('[ValidationExpressionSelector]: %o', props);
  console.info('#'.repeat(60));
  if (props.expresionValues.expresions.length > 0) {
    console.info();
    console.info('-'.repeat(60));
    console.info('ValidationExpresionGroup %o: ');
    return <ValidationExpresionGroup {...props} />;
  }
  console.info();
  console.info('-'.repeat(60));
  console.info('ValidationExpresion');
  return <ValidationExpresion {...props} />;
};
