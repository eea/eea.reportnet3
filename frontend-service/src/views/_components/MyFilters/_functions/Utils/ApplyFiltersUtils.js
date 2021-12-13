import dayjs from 'dayjs';

import { TextUtils } from 'repositories/_utils/TextUtils';

const { areEquals } = TextUtils;

const getEndOfDay = date => new Date(dayjs(date).endOf('day').format()).getTime();

const getStartOfDay = date => new Date(dayjs(date).startOf('day').format()).getTime();

const applyDates = ({ filterBy, filterByKeys, item }) => {
  const filteredKeys = filterByKeys.DATE.filter(key => Object.keys(filterBy).includes(key));

  for (let index = 0; index < filteredKeys.length; index++) {
    const filteredKey = filteredKeys[index];
    const dates = filterBy[filteredKey];
    const value = new Date(item[filteredKey]).getTime();

    if (dates[0] && !dates[1]) return value >= getStartOfDay(dates[0]) && getEndOfDay(dates[0]) >= value;

    if (dates[0] && dates[1]) return value >= getStartOfDay(dates[0]) && getEndOfDay(dates[1]) >= value;
  }

  return true;
};

const applyInputs = ({ filterBy, filterByKeys, item }) => {
  const filteredKeys = filterByKeys.INPUT.filter(key => Object.keys(filterBy).includes(key));

  for (let index = 0; index < filteredKeys.length; index++) {
    const filteredKey = filteredKeys[index];

    if (!areEquals(filterBy[filteredKey], '')) {
      if (!item[filteredKey].toLowerCase().includes(filterBy[filteredKey].toLowerCase())) {
        return false;
      }
    }
  }

  return true;
};

const applyMultiSelects = ({ filterBy, filterByKeys, item }) => {
  const filteredKeys = filterByKeys.MULTI_SELECT.filter(key => Object.keys(filterBy).includes(key));

  for (let index = 0; index < filteredKeys.length; index++) {
    const filteredKey = filteredKeys[index];

    if (!areEquals(filterBy[filteredKey], '') && filterBy[filteredKey].length > 0) {
      if (!filterBy[filteredKey].includes(item[filteredKey].toUpperCase())) {
        return false;
      }
    }
  }

  return true;
};

export const ApplyFiltersUtils = { applyDates, applyInputs, applyMultiSelects };
