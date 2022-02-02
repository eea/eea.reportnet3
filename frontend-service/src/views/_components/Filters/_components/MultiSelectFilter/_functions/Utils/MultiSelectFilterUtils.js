import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';

const getOptionsTypes = (data, nestedOptionKey) => {
  const options = uniq(data.map(item => item[nestedOptionKey])).filter(item => !isNil(item));

  return options.map(option => ({ type: option, value: option }));
};

export const MultiSelectFilterUtils = { getOptionsTypes };
