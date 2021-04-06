import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import sortBy from 'lodash/sortBy';
import uniq from 'lodash/uniq';

const getCheckboxFilterInitialState = checkboxOptions => {
  const initialCheckboxes = [];
  !isEmpty(checkboxOptions) &&
    checkboxOptions.forEach(checkboxOption => {
      initialCheckboxes.push({ property: checkboxOption, isChecked: false });
    });

  return initialCheckboxes;
};

const getCheckboxState = (checkboxes, property) => {
  checkboxes.forEach(checkboxOption => {
    if (checkboxOption.property === property) checkboxOption.isChecked = !checkboxOption.isChecked;
  });
  return checkboxes;
};

const getFilteredSelectedOptions = (filteredData, property, selectedOptions) => {
  const filterOptionsByProperty = filteredData.map(filterOption => filterOption[property].toUpperCase());

  return selectedOptions.filter(labelOption => filterOptionsByProperty.includes(labelOption));
};

const getOptionsNames = options => {
  const separateOptions = { checkbox: [], date: [], dropdown: [], input: [], multiselect: [] };

  options.forEach(option => {
    const names = option.properties.map(property => property.name);
    separateOptions[option.type] = [...separateOptions[option.type], ...names];
  });

  return separateOptions;
};

const getFilterInitialState = (data, input, multiselect, date, dropdown, checkbox = [], filterByList) => {
  if (filterByList) return filterByList;

  const filterByGroup = input.concat(multiselect, date, dropdown, checkbox);
  const filterBy = filterByGroup.reduce((obj, key) => Object.assign(obj, { [key]: '' }), {});

  if (multiselect) {
    multiselect.forEach(selectOption => {
      const selectItems = uniq(data.map(item => item[selectOption]));
      const validSelectItems = selectItems.filter(option => !isNil(option));
      for (let i = 0; i < validSelectItems.length; i++) {
        filterBy[selectOption] = [];
      }
    });
  }

  if (checkbox) {
    checkbox.forEach(checkboxOption => {
      const checkboxItems = uniq(data.map(item => item[checkboxOption]));
      const validCheckboxItems = checkboxItems.filter(option => !isNil(option));
      for (let i = 0; i < validCheckboxItems.length; i++) {
        filterBy[checkboxOption] = [];
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

const getLabelInitialState = (input, multiselect, date, dropdown, checkbox = [], filteredBy) => {
  const labelByGroup = input.concat(multiselect, date, dropdown, checkbox);
  return labelByGroup.reduce((obj, key) => Object.assign(obj, { [key]: !isEmpty(filteredBy[key]) }), {});
};

const getOptionTypes = (data, option, list, order) => {
  if (list && list[option]) {
    return list[option].map(item => ({
      type: item.acronym ? `${item.acronym} - ${item.name}` : item.name,
      value: item.id
    }));
  } else {
    const optionItems = uniq(data.map(item => item[option]));
    const filteredOptionItems = optionItems.filter(option =>
      typeof option === 'boolean' ? option : !isNil(option) && !isEmpty(option)
    );
    const orderedOptions = filteredOptionItems.includes('INFO' || 'WARNING' || 'ERROR' || 'BLOCKER')
      ? order(filteredOptionItems)
      : filteredOptionItems;
    const validOptionItems = orderedOptions.some(item => typeof item === 'boolean') ? [true, false] : orderedOptions;
    for (let i = 0; i < validOptionItems.length; i++) {
      const template = [];
      validOptionItems.forEach(item => {
        if (option === 'isCorrect' && item) {
          template.push({ type: 'VALID', value: item });
        } else if (option === 'isCorrect' && !item) {
          template.push({ type: 'INVALID', value: item });
        } else if (option === 'enabled' && item) {
          template.push({ type: 'ENABLED', value: item });
        } else if (option === 'enabled' && !item) {
          template.push({ type: 'DISABLED', value: item });
        } else if (option === 'automatic' && item) {
          template.push({ type: 'AUTOMATIC', value: item });
        } else if (option === 'automatic' && !item) {
          template.push({ type: 'MANUAL', value: item });
        } else if (option === 'userRole') {
          template.push({
            type: item.toString().replace('_', ' ').toUpperCase(),
            value: item.toString().toUpperCase()
          });
        } else {
          template.push({ type: item.toString().toUpperCase(), value: item.toString().toUpperCase() });
        }
      });
      return sortBy(template, 'type');
    }
  }
};

const getValidationsOptionTypes = (data, option) => {
  const optionsItems = data.filter(filterType => filterType.type === option);
  const validOptions = optionsItems.map(optionItem => optionItem.value);

  for (let i = 0; i < validOptions.length; i++) {
    const template = [];
    validOptions.forEach(item => {
      if (option === 'fieldSchemaName' || option === 'tableSchemaName') {
        !isNil(item) && template.push({ type: item, value: item });
      } else {
        !isNil(item) && template.push({ type: item.toUpperCase(), value: item.toUpperCase() });
      }
    });
    return sortBy(template, 'type');
  }
};

const getSelectedKeys = (state, select, selectOptions = []) => {
  return Object.keys(state.filterBy).filter(key => key !== select && selectOptions.includes(key));
};

export const FiltersUtils = {
  getCheckboxFilterInitialState,
  getCheckboxState,
  getFilterInitialState,
  getFilterKeys,
  getLabelInitialState,
  getOptionTypes,
  getValidationsOptionTypes,
  getSelectedKeys,
  getFilteredSelectedOptions,
  getOptionsNames
};
