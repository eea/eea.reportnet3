import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

const parseSortField = sortField => {
  if (isNil(sortField) || isEmpty(sortField)) {
    return undefined;
  }

  return sortField;
};

export const JobsStatusesUtils = {
  parseSortField
};