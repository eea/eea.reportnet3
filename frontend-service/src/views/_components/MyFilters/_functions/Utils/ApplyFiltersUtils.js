import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';

import { TextUtils } from 'repositories/_utils/TextUtils';

const { areEquals } = TextUtils;

const getEndOfDay = date => new Date(dayjs(date).endOf('day').format()).getTime();

const getStartOfDay = date => new Date(dayjs(date).startOf('day').format()).getTime();

const applyDates = ({ filterBy, filterByKeys, item }) => {
  const checkResultDates = (previousValue, currentValue) => previousValue && currentValue;
  const filteredKeys = filterByKeys.DATE.filter(key => Object.keys(filterBy).includes(key));

  let result = [];

  for (let index = 0; index < filteredKeys.length; index++) {
    const filteredKey = filteredKeys[index];
    const dates = filterBy[filteredKey];
    const value = new Date(item[filteredKey]).getTime();

    if (dates[0] && !dates[1]) result.push(value >= getStartOfDay(dates[0]) && getEndOfDay(dates[0]) >= value);

    if (dates[0] && dates[1]) result = result.push(value >= getStartOfDay(dates[0]) && getEndOfDay(dates[1]) >= value);
  }

  if (result.length > 0) {
    return result.reduce(checkResultDates);
  }

  return true;
};

const applyInputs = ({ filterBy, filterByKeys, item }) => {
  const filteredKeys = filterByKeys.INPUT.filter(key => Object.keys(filterBy).includes(key));

  return filteredKeys.every(
    filteredKey =>
      areEquals(filterBy[filteredKey], '') ||
      item[filteredKey].toLowerCase().includes(filterBy[filteredKey].toLowerCase())
  );
};

const applyMultiSelects = ({ filterBy, filterByKeys, item }) => {
  const filteredKeys = filterByKeys.MULTI_SELECT.filter(key => Object.keys(filterBy).includes(key));

  return filteredKeys.every(
    filteredKey => isEmpty(filterBy[filteredKey]) || filterBy[filteredKey].includes(item[filteredKey].toUpperCase())
  );
};

export const ApplyFiltersUtils = { applyDates, applyInputs, applyMultiSelects };
