import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

export const checkComparisonSQLsentence = sentence => {
  return !(isNil(sentence) || isEmpty(sentence));
};
