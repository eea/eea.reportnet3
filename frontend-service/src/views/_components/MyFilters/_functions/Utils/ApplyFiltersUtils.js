import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { TextUtils } from 'repositories/_utils/TextUtils';

const { areEquals } = TextUtils;

const getEndOfDay = date => new Date(dayjs(date).endOf('day').format()).getTime();
const getStartOfDay = date => new Date(dayjs(date).startOf('day').format()).getTime();

const applyCheckBox = ({ filterBy, filterByKeys, item }) => {
  const filteredKeys = filterByKeys.CHECKBOX.filter(key => Object.keys(filterBy).includes(key));
  return filteredKeys.every(
    filteredKey => !filterBy[filteredKey] || item[filteredKey]?.toString().includes(filterBy[filteredKey]?.toString())
  );
};
const applyDates = ({ filterBy, filterByKeys, item }) => {
  const filteredKeys = filterByKeys.DATE.filter(key => Object.keys(filterBy).includes(key));

  if (isEmpty(filteredKeys)) {
    return true;
  }

  return filteredKeys
    .map(filteredKey => {
      const dates = filterBy[filteredKey];
      const value = new Date(item[filteredKey]).getTime();
      if (dates[0] && !dates[1]) {
        return value >= getStartOfDay(dates[0]) && getEndOfDay(dates[0]) >= value;
      } else if (dates[0] && dates[1]) {
        return value >= getStartOfDay(dates[0]) && getEndOfDay(dates[1]) >= value;
      } else {
        return true;
      }
    })
    .reduce((previousValue, currentValue) => previousValue && currentValue);
};

const applyInputs = ({ filterBy, filterByKeys, item, filterByNestedKeys }) => {
  const checkInput = (item, filteredKey) => item.toLowerCase().includes(filterBy[filteredKey].toLowerCase());

  const checkInputArray = (itemsArray, filteredKey, index) => {
    const everyCheck = itemsArray.map(itemArray => checkInput(itemArray[filteredNestedKeys[index]], filteredKey));

    if (isEmpty(everyCheck)) {
      return false;
    } else {
      return everyCheck.some(item => item);
    }
  };

  const filteredKeys = filterByKeys.INPUT.filter(key => Object.keys(filterBy).includes(key));
  const filteredNestedKeys = filterByNestedKeys.INPUT;

  return filteredKeys.every(
    (filteredKey, index) =>
      areEquals(filterBy[filteredKey], '') ||
      (Array.isArray(item[filteredKey])
        ? checkInputArray(item[filteredKey], filteredKey, index)
        : checkInput(item[filteredKey], filteredKey))
  );
};

const applyMultiSelects = ({ filterBy, filterByKeys, item }) => {
  const filteredKeys = filterByKeys.MULTI_SELECT.filter(key => Object.keys(filterBy).includes(key));

  return filteredKeys.every(
    filteredKey => isEmpty(filterBy[filteredKey]) || filterBy[filteredKey].includes(item[filteredKey])
  );
};

const applySearch = ({ filterByKeys, item, value }) => {
  const filteredKeys = filterByKeys.SEARCH.filter(key => key);

  return (
    isEmpty(filteredKeys) ||
    filteredKeys.some(
      key => areEquals(value, '') || (!isNil(item[key]) && item[key].toLowerCase().includes(value.toLowerCase()))
    )
  );
};

export const ApplyFiltersUtils = { applyCheckBox, applyDates, applyInputs, applyMultiSelects, applySearch };
