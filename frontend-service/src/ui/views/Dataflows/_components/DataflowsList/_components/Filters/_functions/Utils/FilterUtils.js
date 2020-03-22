import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';

const checkDates = (betweenDates, data) => {
  if (!isEmpty(betweenDates)) {
    const dates = betweenDates.map(date => new Date(date).getTime() / 1000);
    return new Date(data).getTime() / 1000 >= dates[0] && new Date(data).getTime() / 1000 <= dates[1];
  }
  return true;
};

const checkFilters = (filteredKeys, dataflow, state) => {
  for (let i = 0; i < filteredKeys.length; i++) {
    if (state.filterBy[filteredKeys[i]].toLowerCase() !== '') {
      if (!dataflow[filteredKeys[i]].toLowerCase().includes(state.filterBy[filteredKeys[i]].toLowerCase())) {
        return false;
      }
    }
  }
  return true;
};

const checkSelected = (state, data, selectedKeys) => {
  for (let index = 0; index < selectedKeys.length; index++) {
    if (!isEmpty(state.filterBy[selectedKeys[index]])) {
      if (!isNil(data[selectedKeys[index]])) {
        if (
          ![...state.filterBy[selectedKeys[index]].map(option => option.toLowerCase())].includes(
            data[selectedKeys[index]].toLowerCase()
          )
        ) {
          return false;
        }
      }
    } else {
      return true;
    }
  }
  return true;
};

const getFilterInitialState = (data, input = [], select = [], date = []) => {
  const filterByGroup = input.concat(select, date);
  const filterBy = filterByGroup.reduce((obj, key) => Object.assign(obj, { [key]: '' }), {});
  if (select) {
    select.forEach(selectOption => {
      const selectItems = uniq(data.map(item => item[selectOption]));
      const validSelectItems = selectItems.filter(option => !isNil(option));
      for (let i = 0; i < validSelectItems.length; i++) {
        filterBy[selectOption] = [];
      }
    });
  }
  if (date) {
    date.forEach(dateOption => {
      filterBy[dateOption] = [];
    });
  }
  return filterBy;
};

const getFilterKeys = (state, filter) =>
  Object.keys(state.filterBy).filter(key => key !== filter && state.inputOptions.includes(key));

const getOptionTypes = (data, option) => {
  const optionItems = uniq(data.map(item => item[option]));
  const validOptionItems = optionItems.filter(option => !isNil(option));
  for (let i = 0; i < validOptionItems.length; i++) {
    const template = [];
    validOptionItems.forEach(item => {
      template.push({ type: item, value: item });
    });
    return template;
  }
};

const getSelectedKeys = (state, select) =>
  Object.keys(state.filterBy).filter(key => key !== select && state.selectOptions.includes(key));

const getYesterdayDate = () => {
  var currentDate = new Date();
  var yesterdayDate = currentDate.setDate(currentDate.getDate() - 1);
  return new Date(yesterdayDate);
};

const onApplyFilters = (filter, filteredKeys, state, selectedKeys, value) => [
  ...state.data.filter(data => {
    if (state.selectOptions.includes(filter) && !isNil(data[filter])) {
      return (
        checkDates(state.filterBy[state.dateOptions], data[state.dateOptions]) &&
        checkFilters(filteredKeys, data, state) &&
        checkSelected(state, data, selectedKeys) &&
        (isEmpty(value) ? true : [...value.map(type => type.toLowerCase())].includes(data[filter].toLowerCase()))
      );
    } else if (state.dateOptions.includes(filter)) {
      const dates = value.map(date => new Date(date).getTime() / 1000);
      return !dates.includes(0) && !isEmpty(dates)
        ? new Date(data[filter]).getTime() / 1000 >= dates[0] &&
            new Date(data[filter]).getTime() / 1000 <= dates[1] &&
            checkFilters(filteredKeys, data, state) &&
            checkSelected(state, data, selectedKeys)
        : checkFilters(filteredKeys, data, state) && checkSelected(state, data, selectedKeys);
    } else {
      return (
        !isNil(data[filter]) &&
        data[filter].toLowerCase().includes(value.toLowerCase()) &&
        checkFilters(filteredKeys, data, state) &&
        checkSelected(state, data, selectedKeys) &&
        checkDates(state.filterBy[state.dateOptions], data[state.dateOptions])
      );
    }
  })
];

export const FilterUtils = {
  getFilterInitialState,
  getFilterKeys,
  getOptionTypes,
  getSelectedKeys,
  getYesterdayDate,
  onApplyFilters
};
