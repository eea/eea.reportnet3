import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import dayjs from 'dayjs';

import { TextUtils } from 'repositories/_utils/TextUtils';

const getStartOfDay = date => new Date(dayjs(date).startOf('day').format()).getTime();
const getEndOfDay = date => new Date(dayjs(date).endOf('day').format()).getTime();

const checkDates = (betweenDates, data) => {
  if (!isEmpty(betweenDates)) {
    const btwDates = [getStartOfDay(betweenDates[0]), getEndOfDay(betweenDates[1])];
    return new Date(data).getTime() >= btwDates[0] && new Date(data).getTime() <= btwDates[1];
  }
  return true;
};

const checkFilters = (filteredKeys = [], dataflow, state) => {
  for (let i = 0; i < filteredKeys.length; i++) {
    if (!TextUtils.areEquals(state.filterBy[filteredKeys[i]], '')) {
      if (!dataflow[filteredKeys[i]].toLowerCase().includes(state.filterBy[filteredKeys[i]].toLowerCase())) {
        return false;
      }
    }
  }
  return true;
};

const checkSearched = (state, data, searchedKeys = []) => {
  const searched = [];
  for (let index = 0; index < searchedKeys.length; index++) {
    if (!isNil(data[searchedKeys[index]])) {
      searched.push(data[searchedKeys[index]].toString().toLowerCase().includes(state.searchBy.toLowerCase()));
    }
  }
  return searched.includes(true);
};

const checkSelected = (state, data, selectedKeys = [], actualFilterBy) => {
  for (let index = 0; index < selectedKeys.length; index++) {
    const selectedKey = selectedKeys[index];
    const value = actualFilterBy ? actualFilterBy[selectedKey] : state.filterBy[selectedKey];

    if (!isEmpty(value)) {
      if (!isNil(data[selectedKey])) {
        const selectedData = data[selectedKey];

        if (Array.isArray(selectedData)) return onApplyGroupFilters(data, selectedKey, state.matchMode, value);
        else {
          const isFiltered = ![...value.map(option => option.toString().toLowerCase())].includes(
            selectedData.toString().toLowerCase()
          );

          if (isFiltered) return false;
        }
      }
    }
  }
  return true;
};

const getSearchKeys = data => {
  if (!isNil(data))
    return Object.keys(Array.isArray(data) && !isEmpty(data) ? data[0] : data).filter(
      item => item !== 'id' && item !== 'key'
    );
};

const onApplyFilters = ({
  actualFilterBy,
  checkbox = [],
  checkedKeys,
  data,
  date,
  filter,
  filteredKeys,
  searchedKeys,
  selectedKeys,
  multiselect,
  state,
  value
}) => {
  const result = data.filter(dataItem => {
    if (
      (multiselect.includes(filter) && !isNil(dataItem[filter])) ||
      (checkbox.includes(filter) && !isNil(dataItem[filter]))
    ) {
      const isApplySelected = onApplySelected(dataItem, filter, state, value);
      const isCheckFilters = onCheckFilters(
        dataItem,
        date,
        filteredKeys,
        searchedKeys,
        selectedKeys,
        checkedKeys,
        state,
        actualFilterBy
      );
      return isApplySelected && isCheckFilters;
    }

    if (date.includes(filter)) {
      let dates;

      isEmpty(value) ? (dates = []) : (dates = [getStartOfDay(value[0]), getEndOfDay(value[1])]);

      return !dates.includes(NaN) && !isEmpty(dates)
        ? new Date(dataItem[filter]).getTime() >= dates[0] &&
            new Date(dataItem[filter]).getTime() <= dates[1] &&
            checkFilters(filteredKeys, dataItem, state) &&
            checkSearched(state, dataItem, searchedKeys) &&
            checkSelected(state, dataItem, selectedKeys, actualFilterBy) &&
            checkSelected(state, dataItem, checkedKeys, actualFilterBy)
        : checkFilters(filteredKeys, dataItem, state) &&
            checkSearched(state, dataItem, searchedKeys) &&
            checkSelected(state, dataItem, selectedKeys, actualFilterBy) &&
            checkSelected(state, dataItem, checkedKeys, actualFilterBy);
    }

    return (
      !isNil(dataItem[filter]) &&
      dataItem[filter].toLowerCase().includes(value.toLowerCase()) &&
      checkFilters(filteredKeys, dataItem, state) &&
      checkSearched(state, dataItem, searchedKeys) &&
      checkSelected(state, dataItem, selectedKeys, actualFilterBy) &&
      checkSelected(state, dataItem, checkedKeys, actualFilterBy) &&
      checkDates(state.filterBy[date], dataItem[date])
    );
  });

  return [...result];
};

const onApplyGroupFilters = (data, filter, matchMode, value) => {
  const resultData = [];
  const dataValues = data[filter].map(item => item.fieldId.toString().toLowerCase());

  if (matchMode) resultData.push(value.every(value => dataValues.includes(value)));
  else {
    for (let index = 0; index < value.length; index++) {
      resultData.push(dataValues.includes(value[index].toString().toLowerCase()));
    }
  }
  return resultData.includes(true);
};

const onApplySearch = (data, searchBy = [], value, state, inputKeys, selectedKeys, checkedKeys) => [
  ...data.filter(data => {
    const searchedParams = !isEmpty(searchBy) ? searchBy : getSearchKeys(data);
    const filteredData = [];
    for (let index = 0; index < searchedParams?.length; index++) {
      if (!isNil(data[searchedParams[index]])) {
        filteredData.push(data[searchedParams[index]].toString().toLowerCase().includes(value.toLowerCase()));
      }
    }
    return (
      filteredData.includes(true) &&
      checkFilters(inputKeys, data, state) &&
      checkSelected(state, data, selectedKeys) &&
      checkSelected(state, data, checkedKeys)
    );
  })
];

const onApplySelected = (data, filter, state, value) => {
  if (!isEmpty(value)) {
    return Array.isArray(data[filter])
      ? onApplyGroupFilters(data, filter, state.matchMode, value)
      : [...value.map(type => type.toString().toLowerCase())].includes(data[filter].toString().toLowerCase());
  }
  return true;
};

const onCheckFilters = (data, date, filteredKeys, searchedKeys, selectedKeys, checkedKeys, state, actualFilterBy) => {
  const isCheckedDates = checkDates(state.filterBy[date], data[date]);
  const isCheckedFilters = checkFilters(filteredKeys, data, state);
  const isCheckedSearchedKeys = checkSearched(state, data, searchedKeys);
  const isCheckedSelectedKeys = checkSelected(state, data, selectedKeys, actualFilterBy);
  const isCheckedCheckedKeys = checkSelected(state, data, checkedKeys, actualFilterBy);

  return isCheckedDates && isCheckedFilters && isCheckedSearchedKeys && isCheckedSelectedKeys && isCheckedCheckedKeys;
};

const onClearLabelState = (input = [], multiselect = [], date = [], dropdown = [], checkbox = []) => {
  const labelByGroup = input.concat(multiselect, date, dropdown, checkbox);
  return labelByGroup.reduce((obj, key) => Object.assign(obj, { [key]: false }), {});
};

export const ApplyFilterUtils = { getSearchKeys, onApplyFilters, onApplySearch, onClearLabelState };
