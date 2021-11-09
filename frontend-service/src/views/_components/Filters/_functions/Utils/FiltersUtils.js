import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import sortBy from 'lodash/sortBy';
import uniq from 'lodash/uniq';

import { config } from 'conf';

const getUserRoleLabel = role => {
  const userRole = Object.values(config.permissions.roles).find(rol => rol.key === role);
  return userRole?.label || role;
};

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

const getFilterKeys = (state, filter, inputOptions = []) => {
  return Object.keys(state.filterBy).filter(key => key !== filter && inputOptions.includes(key));
};

const getLabelInitialState = (input, multiselect, date, dropdown, checkbox = [], filteredBy) => {
  const labelByGroup = input.concat(multiselect, date, dropdown, checkbox);
  return labelByGroup.reduce((obj, key) => Object.assign(obj, { [key]: !isEmpty(filteredBy[key]) }), {});
};

const getOptionsNames = options => {
  const separateOptions = { checkbox: [], date: [], dropdown: [], input: [], multiselect: [] };

  options.forEach(option => {
    const names = option.properties.map(property => property.name);
    separateOptions[option.type] = [...separateOptions[option.type], ...names];
  });

  return separateOptions;
};

const getOptionsTemplate = (filteredOptions, property) => {
  const template = [];

  filteredOptions.forEach(option => {
    switch (property) {
      case 'automatic':
        template.push({ type: option ? 'AUTOMATIC' : 'MANUAL', value: option });
        break;
      case 'enabled':
        template.push({ type: option ? 'ENABLED' : 'DISABLED', value: option });
        break;
      case 'isCorrect':
        template.push({ type: option ? 'VALID' : 'INVALID', value: option });
        break;
      case 'userRole':
        template.push({ type: option, value: option });
        break;
      case 'role':
        template.push({ type: getUserRoleLabel(option), value: option });
        break;
      case 'restrictFromPublic':
        template.push({ type: option?.toString().toUpperCase(), value: !option });
        break;
      default:
        template.push({ type: option?.toString().toUpperCase(), value: option?.toString().toUpperCase() });
    }
  });

  return template;
};

const getOptionsTypes = (data, property, list, sortErrors) => {
  if (list && list[property]) {
    return list[property].map(item => {
      return { type: item.acronym ? `${item.acronym} - ${item.name}` : item.name, value: item.id };
    });
  }

  const options = uniq(data.map(item => item[property])).filter(onFilterBooleanOptions);
  const sortedOptions = options.includes('INFO' || 'WARNING' || 'ERROR' || 'BLOCKER')
    ? sortErrors(options).reverse()
    : options;
  const filteredOptions = sortedOptions.some(item => typeof item === 'boolean') ? [true, false] : sortedOptions;

  return getOptionsTemplate(filteredOptions, property);
};

const getSelectedKeys = (state, select, selectOptions = []) => {
  return Object.keys(state.filterBy).filter(key => key !== select && selectOptions.includes(key));
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

const onFilterBooleanOptions = option => (typeof option !== 'boolean' ? !isNil(option) && !isEmpty(option) : true);

export const FiltersUtils = {
  getCheckboxFilterInitialState,
  getCheckboxState,
  getFilteredSelectedOptions,
  getFilterInitialState,
  getFilterKeys,
  getLabelInitialState,
  getOptionsNames,
  getOptionsTypes,
  getSelectedKeys,
  getValidationsOptionTypes
};
