import isEmpty from 'lodash/isEmpty';
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
    if (
      ![...state.filterBy[selectedKeys[index]].map(option => option.value.toLowerCase())].includes(
        data[selectedKeys[index]].toLowerCase()
      )
    ) {
      return false;
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
      for (let i = 0; i < selectItems.length; i++) {
        const data = [];
        selectItems.forEach(item => {
          data.push({ type: item, value: item });
        });
        filterBy[selectOption] = data;
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
  for (let i = 0; i < optionItems.length; i++) {
    const template = [];
    optionItems.forEach(item => {
      template.push({ type: item, value: item });
    });
    return template;
  }
};

const getSelectedKeys = (state, select) =>
  Object.keys(state.filterBy).filter(key => key !== select && state.selectOptions.includes(key));

const onApplyFilters = (filter, filteredKeys, state, selectedKeys, value) => [
  ...state.data.filter(data => {
    if (state.selectOptions.includes(filter)) {
      return (
        [...value.map(type => type.value.toLowerCase())].includes(data[filter].toLowerCase()) &&
        checkDates(state.filterBy[state.dateOptions], data[state.dateOptions]) &&
        checkFilters(filteredKeys, data, state) &&
        checkSelected(state, data, selectedKeys)
      );
    } else if (state.dateOptions.includes(filter)) {
      const dates = value.map(date => new Date(date).getTime() / 1000);
      return !dates.includes(0)
        ? new Date(data[filter]).getTime() / 1000 >= dates[0] &&
            new Date(data[filter]).getTime() / 1000 <= dates[1] &&
            checkFilters(filteredKeys, data, state) &&
            checkSelected(state, data, selectedKeys)
        : checkFilters(filteredKeys, data, state) && checkSelected(state, data, selectedKeys);
    } else {
      return (
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
  onApplyFilters
};
