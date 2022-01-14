import { isNil } from 'lodash';
import isDate from 'lodash/isDate';
import isEmpty from 'lodash/isEmpty';
import uniq from 'lodash/uniq';

const parseDateValues = values => {
  if (!values) {
    return [];
  }

  return values.map(value => {
    if (!value) {
      return null;
    }

    return isDate(value) ? value.getTime() : new Date(value);
  });
};

const getPositionLabelAnimationDate = (labelsAnimationDate, key) => {
  for (const position in labelsAnimationDate) {
    const keyLabelAnimation = Object.keys(labelsAnimationDate[position])[0];
    if (keyLabelAnimation === key) {
      return position;
    }
  }
  return undefined;
};

const getLabelsAnimationDateInitial = (options, filterBy) =>
  options.filter(option => option?.type === 'DATE').map(option => ({ [option.key]: !isEmpty(filterBy[option.key]) }));

const getOptionsTypes = (data, nestedOptionKey) => {
  const options = uniq(data.map(item => item[nestedOptionKey])).filter(item => !isNil(item));

  return options.map(option => ({ type: option, value: option }));
};

export const FiltersUtils = {
  getLabelsAnimationDateInitial,
  getOptionsTypes,
  getPositionLabelAnimationDate,
  parseDateValues
};
