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
  filteredOptions.forEach(option => {
    switch (key) {
      // case 'userRole':
      //   template.push({ type: option, value: option });
      //   break;
      default:
        template.push({ type: option?.toString().toUpperCase(), value: option?.toString().toUpperCase() });
    }
  });

  return template;
};

const onFilterBooleanOptions = option => (typeof option !== 'boolean' ? !isNil(option) && !isEmpty(option) : true);

const getOptionsTypes = (data, nestedOptionKey, list, sortErrors) => {
  // if (list && list[nestedOptionKey]) {
  //   return list[nestedOptionKey].map(item => {
  //     return { type: item.acronym ? `${item.acronym} - ${item.name}` : item.name, value: item.id };
  //   });
  // }
  const options = uniq(data.map(item => item[nestedOptionKey])).filter(onFilterBooleanOptions);
  // const sortedOptions = options.includes('INFO' || 'WARNING' || 'ERROR' || 'BLOCKER')
  //   ? sortErrors(options).reverse()
  //   : options;
  // const filteredOptions = sortedOptions.some(item => typeof item === 'boolean') ? [true, false] : sortedOptions;

  return getOptionsByKeyNestedOption(options, nestedOptionKey);
};

export const MyFiltersUtils = { deepIncludes, getEndOfDay, getStartOfDay, parseDateValues, getOptionsTypes };
