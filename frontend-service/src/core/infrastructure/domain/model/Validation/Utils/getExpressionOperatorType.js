import { config } from 'conf';

export const getExpressionOperatorType = (operator, type = 'field') => {
  const { validations } = config;
  const confOperators = validations[`${type}OperatorsTypesFromDTO`];

  const selectedOperators = confOperators.filter(operatorTypeObject => {
    return operatorTypeObject.operators.includes(operator) && operatorTypeObject;
  });

  const [selectedOperator] = selectedOperators;
  return selectedOperator.operatorType;
};
