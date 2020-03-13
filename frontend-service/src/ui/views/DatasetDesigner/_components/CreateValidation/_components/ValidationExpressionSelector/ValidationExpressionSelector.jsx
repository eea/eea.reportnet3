import { ValidationExpresion } from 'ui/views/DatasetDesigner/_components/CreateValidation/_components/ValidationExpresion';
import { ValidationExpresionGroup } from 'ui/views/DatasetDesigner/_components/CreateValidation/_components/ValidationExpresionGroup';
export const ValidationExpressionSelector = props => {
  if (props.expresionValues.expresions.lenght > 0) {
    <ValidationExpresionGroup {...props} />;
  } else {
    <ValidationExpresion {...props} />;
  }
};
