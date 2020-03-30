import { config } from 'conf';

export const getExpressionOperatorType = operator => {
  if (!['SEQ', 'SEQIC'].includes(operator)) {
    return config.validations.operatorTypes.number.option.value;
  } else {
    return config.validations.operatorTypes.string.option.value;
  }
};
