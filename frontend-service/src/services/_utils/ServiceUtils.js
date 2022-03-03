import isNil from 'lodash/isNil';

const getSortOrder = sortOrder => {
  if (sortOrder === -1) {
    return 0;
  } else if (isNil(sortOrder)) {
    return undefined;
  } else {
    return sortOrder;
  }
};

export const ServiceUtils = {
  getSortOrder
};
