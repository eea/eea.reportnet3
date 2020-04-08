import { config } from 'conf';

export const getExpressionOperatorType = operator => {
  const {
    validations: { operatorsTypesFromDTO }
  } = config;
  const selectedOperators = operatorsTypesFromDTO.filter(operatorTypeObject => {
    if (operatorTypeObject.operators.includes(operator)) {
      return operatorTypeObject;
    }
  });
  const [selectedOperator] = selectedOperators;
  return selectedOperator.operatorType;
};
