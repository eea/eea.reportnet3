import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import uniq from 'lodash/uniq';

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
    const optionItems = uniq(data.map(item => item[option]));
    const validOptionItems = optionItems.filter(option => !isNil(option));
    for (let i = 0; i < validOptionItems.length; i++) {
      const template = [];
      validOptionItems.forEach(item => {
        if (option === 'isCorrect' && item === true) {
          template.push({ type: 'CORRECT', value: item });
        } else if (option === 'isCorrect' && item === false) {
          template.push({ type: 'INCORRECT', value: item });
        } else if (option === 'enabled' && item === true) {
          template.push({ type: 'ENABLED', value: item });
        } else if (option === 'enabled' && item === false) {
          template.push({ type: 'DISABLED', value: item });
        } else if (option === 'automatic' && item === true) {
          template.push({ type: 'AUTOMATIC', value: item });
        } else if (option === 'automatic' && item === false) {
          template.push({ type: 'MANUAL', value: item });
        } else {
          template.push({ type: item.toString().toUpperCase(), value: item.toString().toUpperCase() });
        }
      });
      return template;
    }
  }
};

const getSelectedKeys = (state, select, selectOptions = []) =>
  Object.keys(state.filterBy).filter(key => key !== select && selectOptions.includes(key));

export const FiltersUtils = {
  getFilterInitialState,
  getFilterKeys,
  getLabelInitialState,
  getOptionTypes,
  getSelectedKeys
};
