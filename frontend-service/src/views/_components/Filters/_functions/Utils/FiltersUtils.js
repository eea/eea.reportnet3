import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { TextUtils } from 'repositories/_utils/TextUtils';

const getEndOfDay = date => new Date(dayjs(date).endOf('day').format()).getTime();
const getStartOfDay = date => new Date(dayjs(date).startOf('day').format()).getTime();

const applyCheckBox = ({ filterBy, filteredKeys = [], item }) =>
  filteredKeys.every(key => !filterBy[key] || item[key]?.toString().includes(filterBy[key]?.toString()));

const applyDates = ({ filterBy, filteredKeys = [], item }) => {
  if (isEmpty(filteredKeys)) {
    return true;
  }

  return filteredKeys
    .map(filteredKey => {
      const dates = filterBy[filteredKey];
      const value = new Date(item[filteredKey]).getTime();

      if (dates[0] && !dates[1]) {
        return value >= getStartOfDay(dates[0]) && getEndOfDay(dates[0]) >= value;
      }

      if (dates[0] && dates[1]) {
        return value >= getStartOfDay(dates[0]) && getEndOfDay(dates[1]) >= value;
      }

      return true;
    })
    .reduce((previousValue, currentValue) => previousValue && currentValue);
};

const applyInputs = ({ filterBy, filteredKeys = [], item }) =>
  filteredKeys.every(key => {
    if (isEmpty(filterBy[key])) {
      return true;
    }

    return item[key].toString().toLowerCase().includes(filterBy[key].toString().toLowerCase());
  });

const applyMultiSelects = ({ filterBy, filteredKeys = [], isStrictMode, item }) =>
  filteredKeys.every(filteredKey => {
    if (isEmpty(filterBy[filteredKey])) {
      return true;
    }

    if (Array.isArray(item[filteredKey])) {
      const nestedItems = item[filteredKey].map(nestedItem => nestedItem.fieldId);

      if (isStrictMode) {
        return filterBy[filteredKey].every(value => nestedItems.includes(value));
      } else {
        return filterBy[filteredKey].some(value => nestedItems.includes(value));
      }
    }

    return filterBy[filteredKey].includes(item[filteredKey]);
  });

const applySearch = ({ filteredKeys = [], item, value }) => {
  if (isEmpty(filteredKeys)) {
    return true;
  }

  return filteredKeys.some(
    key => isEmpty(value) || (!isNil(item[key]) && item[key].toLowerCase().includes(value.toLowerCase()))
  );
};

const getIsFiltered = filterBy => {
  if (isEmpty(filterBy)) {
    return false;
  }

  return Object.values(filterBy).some(
    element =>
      (Array.isArray(element) && element.length > 0) ||
      (!Array.isArray(element) && !TextUtils.areEquals(element.trim(), ''))
  );
};

export const FiltersUtils = { applyCheckBox, applyDates, applyInputs, applyMultiSelects, applySearch, getIsFiltered };
