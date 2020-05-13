import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import moment from 'moment';

const checkDates = (betweenDates, data) => {
  if (!isEmpty(betweenDates)) {
    const btwDates = [getStartOfDay(betweenDates[0]), getEndOfDay(betweenDates[1])];
    return new Date(data).getTime() / 1000 >= btwDates[0] && new Date(data).getTime() / 1000 <= btwDates[1];
  }
  return true;
};

const checkFilters = (filteredKeys = [], dataflow, state) => {
  for (let i = 0; i < filteredKeys.length; i++) {
    if (state.filterBy[filteredKeys[i]].toLowerCase() !== '') {
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

const checkSelected = (state, data, selectedKeys = []) => {
  for (let index = 0; index < selectedKeys.length; index++) {
    if (!isEmpty(state.filterBy[selectedKeys[index]])) {
      if (!isNil(data[selectedKeys[index]])) {
        if (
          ![...state.filterBy[selectedKeys[index]].map(option => option.toString().toLowerCase())].includes(
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

const getEndOfDay = date => new Date(moment(date).endOf('day').format()).getTime() / 1000;

const getSearchKeys = data => {
  if (!isNil(data))
    return Object.keys(Array.isArray(data) ? data[0] : data).filter(item => item !== 'id' && item !== 'key');
};

const getStartOfDay = date => new Date(moment(date).startOf('day').format()).getTime() / 1000;

const onApplyFilters = ({
  dateOptions = [],
  filter,
  filteredKeys,
  searchedKeys,
  selectedKeys,
  selectOptions = [],
  state,
  value
}) => [
  ...state.data.filter(data => {
    if (selectOptions.includes(filter) && !isNil(data[filter])) {
      return (
        checkDates(state.filterBy[dateOptions], data[dateOptions]) &&
        checkFilters(filteredKeys, data, state) &&
        checkSearched(state, data, searchedKeys) &&
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
            checkSearched(state, data, searchedKeys) &&
            checkSelected(state, data, selectedKeys)
        : checkFilters(filteredKeys, data, state) &&
            checkSearched(state, data, searchedKeys) &&
            checkSelected(state, data, selectedKeys);
    } else {
      return (
        !isNil(data[filter]) &&
        data[filter].toLowerCase().includes(value.toLowerCase()) &&
        checkFilters(filteredKeys, data, state) &&
        checkSearched(state, data, searchedKeys) &&
        checkSelected(state, data, selectedKeys) &&
        checkDates(state.filterBy[dateOptions], data[dateOptions])
      );
    }
  })
];

const onApplySearch = (data, searchBy = [], value, state, inputKeys, selectedKeys) => [
  ...data.filter(data => {
    const searchedParams = !isEmpty(searchBy) ? searchBy : getSearchKeys(data);
    const filteredData = [];
    for (let index = 0; index < searchedParams.length; index++) {
      if (!isNil(data[searchedParams[index]])) {
        filteredData.push(data[searchedParams[index]].toString().toLowerCase().includes(value.toLowerCase()));
      }
    }
    return (
      filteredData.includes(true) && checkFilters(inputKeys, data, state) && checkSelected(state, data, selectedKeys)
    );
  })
];

const onClearLabelState = (input = [], select = [], date = [], dropDown = []) => {
  const labelByGroup = input.concat(select, date, dropDown);
  return labelByGroup.reduce((obj, key) => Object.assign(obj, { [key]: false }), {});
};

export const ApplyFilterUtils = { getSearchKeys, onApplyFilters, onApplySearch, onClearLabelState };
