import isDate from 'lodash/isDate';
import uniq from 'lodash/uniq';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

const deepIncludes = ({ entries, value }) => entries.toLowerCase().includes(value.toLowerCase());

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

const getLabelsAnimationDateInitial = options => {
  const result = options
    .filter(option => option?.type === 'DATE')
    .map(option => {
      return { [option.key]: false };
    });
  return result;
};

const getOptionsByKeyNestedOption = (filteredOptions, key) => {
  const template = [];
  filteredOptions.forEach(option =>
    template.push({ type: option?.toString().toUpperCase(), value: option?.toString().toUpperCase() })
  );

  return template;
};

const onFilterBooleanOptions = option => (typeof option !== 'boolean' ? !isNil(option) && !isEmpty(option) : true);

const getOptionsTypes = (data, nestedOptionKey) => {
  const options = uniq(data.map(item => item[nestedOptionKey])).filter(onFilterBooleanOptions);
  return getOptionsByKeyNestedOption(options, nestedOptionKey);
};

export const FiltersUtils = {
  deepIncludes,
  getOptionsTypes,
  parseDateValues,
  getLabelsAnimationDateInitial,
  getPositionLabelAnimationDate
};
