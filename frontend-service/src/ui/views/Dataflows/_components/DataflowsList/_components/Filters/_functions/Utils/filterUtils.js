import uniq from 'lodash/uniq';

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

export const FilterUtils = {
  getFilterInitialState,
  getOptionTypes
};
