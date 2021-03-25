import { config } from 'conf';

export const getExpressionOperatorType = (operator, type = 'field') => {
  const { validations } = config;
  const confOperators = validations[`${type}OperatorsTypesFromDTO`];

  const selectedOperators = confOperators.filter(operatorTypeObject => {
    if (operatorTypeObject.operators.includes(operator)) {
      return operatorTypeObject;
    }
    return false;
  });

  const [selectedOperator] = selectedOperators;
  return selectedOperator.operatorType;
};
