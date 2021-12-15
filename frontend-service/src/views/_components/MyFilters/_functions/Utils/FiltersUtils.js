import isDate from 'lodash/isDate';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';

const parseDateValues = values => {
  if (!values) return [];

  return values.map(value => {
    if (!value) return null;

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

const getLabelsAnimationDateInitial = (options, filterBy) => {
  return options
    .filter(option => option?.type === 'DATE')
    .map(option => ({
      [option.key]: !isEmpty(filterBy[option.key])
    }));
};

const getOptionsByKeyNestedOption = (filteredOptions, key) => {
  return filteredOptions.map(option => ({
    type: option?.toString().toUpperCase(),
    value: option?.toString().toUpperCase()
  }));
};

const onFilterBooleanOptions = option => (typeof option !== 'boolean' ? !isNil(option) && !isEmpty(option) : true);

const getOptionsTypes = (data, nestedOptionKey) => {
  const options = uniq(data.map(item => item[nestedOptionKey])).filter(onFilterBooleanOptions);
  return getOptionsByKeyNestedOption(options, nestedOptionKey);
};

export const FiltersUtils = {
  getLabelsAnimationDateInitial,
  getOptionsTypes,
  getPositionLabelAnimationDate,
  parseDateValues
};
