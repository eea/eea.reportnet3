import dayjs from 'dayjs';
import isDate from 'lodash/isDate';
import uniq from 'lodash/uniq';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

const deepIncludes = ({ entries, value }) => entries.toLowerCase().includes(value.toLowerCase());

const getEndOfDay = date => new Date(dayjs(date).endOf('day').format()).getTime();

const getStartOfDay = date => new Date(dayjs(date).startOf('day').format()).getTime();

const parseDateValues = values => {
  if (!values) return [];

  return values.map(value => {
    if (!value) return null;

    return isDate(value) ? value.getTime() : new Date(value);
  });
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

export const FiltersUtils = { deepIncludes, getEndOfDay, getOptionsTypes, getStartOfDay, parseDateValues };
