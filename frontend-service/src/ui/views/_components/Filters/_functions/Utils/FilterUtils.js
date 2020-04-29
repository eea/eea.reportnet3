import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';
import moment from 'moment';

const checkDates = (betweenDates, data) => {
  if (!isEmpty(betweenDates)) {
    const btwDates = [getStartOfDay(betweenDates[0]), getEndOfDay(betweenDates[1])];
    return new Date(data).getTime() / 1000 >= btwDates[0] && new Date(data).getTime() / 1000 <= btwDates[1];
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
            data[selectedKeys[index]].toString().toLowerCase()
          )
        ) {
          return false;
        }
      }
    }
  }
  return true;
};

const getFilterInitialState = (data, input = [], select = [], date = [], dropDown = [], filterByList) => {
  if (filterByList) return filterByList;

  const filterByGroup = input.concat(select, date, dropDown);
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

const getEndOfDay = date => new Date(moment(date).endOf('day').format()).getTime() / 1000;

const getFilterKeys = (state, filter, inputOptions = []) =>
  Object.keys(state.filterBy).filter(key => key !== filter && inputOptions.includes(key));

const getLabelInitialState = (input = [], select = [], date = [], dropDown = [], filteredBy) => {
  const labelByGroup = input.concat(select, date, dropDown);
  return labelByGroup.reduce((obj, key) => Object.assign(obj, { [key]: !isEmpty(filteredBy[key]) }), {});
};

const getOptionTypes = (data, option, list) => {
  if (list) {
    return list[option].map(item => ({
      type: item.acronym ? `${item.acronym} - ${item.name}` : item.name,
      value: item.id
    }));
  } else {
    data.forEach(element => {
      if (element.isCorrect === true || element.isCorrect === 'CORRECT') {
        element.isCorrect = 'CORRECT';
      } else {
        element.isCorrect = 'INCORRECT';
      }

      if (element.enabled === true || element.enabled === 'ENABLED') {
        element.enabled = 'ENABLED';
      } else {
        element.enabled = 'DISABLED';
      }
    });

    const optionItems = uniq(data.map(item => item[option]));
    const validOptionItems = optionItems.filter(option => !isNil(option));
    for (let i = 0; i < validOptionItems.length; i++) {
      const template = [];
      validOptionItems.forEach(item => {
        template.push({ type: item.toString().toUpperCase(), value: item.toString().toUpperCase() });
      });
      return template;
    }
  }
};

const getSelectedKeys = (state, select, selectOptions = []) =>
  Object.keys(state.filterBy).filter(key => key !== select && selectOptions.includes(key));

const getStartOfDay = date => new Date(moment(date).startOf('day').format()).getTime() / 1000;

const getYesterdayDate = () => {
  var currentDate = new Date();
  var yesterdayDate = currentDate.setDate(currentDate.getDate() - 1);
  return new Date(yesterdayDate);
};

const onApplyFilters = (filter, filteredKeys, state, selectedKeys, value, dateOptions = [], selectOptions = []) => [
  ...state.data.filter(data => {
    if (selectOptions.includes(filter) && !isNil(data[filter])) {
      return (
        checkDates(state.filterBy[dateOptions], data[dateOptions]) &&
        checkFilters(filteredKeys, data, state) &&
        checkSelected(state, data, selectedKeys) &&
        (isEmpty(value)
          ? true
          : [...value.map(type => type.toString().toLowerCase())].includes(data[filter].toString().toLowerCase()))
      );
    } else if (dateOptions.includes(filter)) {
      let dates;
      isEmpty(value) ? (dates = []) : (dates = [getStartOfDay(value[0]), getEndOfDay(value[1])]);
      return !dates.includes(NaN) && !isEmpty(dates)
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
        checkDates(state.filterBy[dateOptions], data[dateOptions])
      );
    }
  })
];

const onClearLabelState = (input = [], select = [], date = [], dropDown = []) => {
  const labelByGroup = input.concat(select, date, dropDown);
  return labelByGroup.reduce((obj, key) => Object.assign(obj, { [key]: false }), {});
};

export const FilterUtils = {
  getFilterInitialState,
  getFilterKeys,
  getLabelInitialState,
  getOptionTypes,
  getSelectedKeys,
  getYesterdayDate,
  onApplyFilters,
  onClearLabelState
};
