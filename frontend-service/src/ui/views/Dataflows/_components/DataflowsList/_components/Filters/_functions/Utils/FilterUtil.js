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
  Object.keys(state.filterBy).filter(
    key => key !== filter && key !== 'status' && key !== 'userRole' && key !== 'expirationDate'
  );

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

const onApplyFilters = (filter, filteredKeys, state, value) => [
  ...state.data.filter(data => {
    if (state.selectOptions.includes(filter)) {
      return (
        [...value.map(type => type.value.toLowerCase())].includes(data[filter].toLowerCase()) &&
        checkFilters(filteredKeys, data, state) &&
        checkDates(state.filterBy.expirationDate, data.expirationDate)
      );
    } else if (state.dateOptions.includes(filter)) {
      const dates = [];
      value.map(date => dates.push(new Date(date).getTime() / 1000));

      if (!dates.includes(0)) {
        return (
          new Date(data[filter]).getTime() / 1000 >= dates[0] &&
          new Date(data[filter]).getTime() / 1000 <= dates[1] &&
          checkFilters(filteredKeys, data, state) &&
          [...state.filterBy.status.map(status => status.value.toLowerCase())].includes(data.status.toLowerCase()) &&
          [...state.filterBy.userRole.map(userRole => userRole.value.toLowerCase())].includes(
            data.userRole.toLowerCase()
          )
        );
      } else {
        return [...state.filteredData];
      }
    } else {
      return (
        data[filter].toLowerCase().includes(value.toLowerCase()) &&
        checkFilters(filteredKeys, data, state) &&
        [...state.filterBy.status.map(status => status.value.toLowerCase())].includes(data.status.toLowerCase()) &&
        [...state.filterBy.userRole.map(userRole => userRole.value.toLowerCase())].includes(
          data.userRole.toLowerCase()
        ) &&
        checkDates(state.filterBy.expirationDate, data.expirationDate)
      );
    }
  })
];

export const FilterUtil = {
  getFilterInitialState,
  getFilterKeys,
  getOptionTypes,
  onApplyFilters
};
