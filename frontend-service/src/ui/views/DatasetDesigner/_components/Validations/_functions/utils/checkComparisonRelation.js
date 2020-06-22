import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import last from 'lodash/last';

export const checkComparisonRelation = links => {
  if (!isNil(links) && links.length > 0) {
    const lastLink = last(links);
    return isEmpty(lastLink.originField) || isEmpty(lastLink.referencedField);
  }
  return true;
};
