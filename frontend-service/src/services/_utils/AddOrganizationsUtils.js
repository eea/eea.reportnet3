import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

const parseSortField = sortField => {
  if (isNil(sortField) || isEmpty(sortField)) {
    return undefined;
  }

  return sortField;
};

const parseGroupId = groupId => {
  if (isNil(groupId)) {
    return undefined;
  }

  return groupId;
};

export const AddOrganizationsUtils = {
  parseGroupId,
  parseSortField
};
