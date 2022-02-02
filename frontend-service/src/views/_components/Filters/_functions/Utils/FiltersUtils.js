import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

const getEndOfDay = date => new Date(dayjs(date).endOf('day').format()).getTime();
const getStartOfDay = date => new Date(dayjs(date).startOf('day').format()).getTime();

const applyCheckBox = ({ filterBy, filteredKeys = [], item }) => {
  return filteredKeys.every(key => {
    return !filterBy[key] || item[key]?.toString().includes(filterBy[key]?.toString());
  });
};

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

const applyInputs = ({ filterBy, filteredKeys = [], item }) => {
  return filteredKeys.every(key => {
    if (isEmpty(filterBy[key])) {
      return true;
    }

    return item[key].toLowerCase().includes(filterBy[key].toLowerCase());
  });
};

const applyMultiSelects = ({ filterBy, filteredKeys = [], isStrictMode, item }) => {
  // return filteredKeys.every(key => isEmpty(filterBy[key]) || filterBy[key].includes(item[key]));

  return filteredKeys.every(filteredKey => {
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
};

const applySearch = ({ filteredKeys = [], item, value }) => {
  if (isEmpty(filteredKeys)) {
    return true;
  }

  return filteredKeys.some(key => {
    return isEmpty(value) || (!isNil(item[key]) && item[key].toLowerCase().includes(value.toLowerCase()));
  });
};

const getIsFiltered = filterBy => {
  if (isEmpty(filterBy)) {
    return false;
  }

  return Object.values(filterBy)
    .map(key => isEmpty(key))
    .includes(false);
};

export const FiltersUtils = { applyCheckBox, applyDates, applyInputs, applyMultiSelects, applySearch, getIsFiltered };
