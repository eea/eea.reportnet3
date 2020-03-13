import uniq from 'lodash/uniq';

const getFilterInitialState = (data, input, select, date) => {
  const filterByGroup = input.concat(select, date);
  const filterBy = filterByGroup.reduce((obj, key) => Object.assign(obj, { [key]: '' }), {});

  select.forEach(element => {
    const selectItems = uniq(data.map(item => item[element]));
    for (let i = 0; i < selectItems.length; i++) {
      const data = [];
      selectItems.forEach(item => {
        data.push({ type: item, value: item });
      });
      filterBy[element] = data;
    }
  });
  return filterBy;
};

const getOrderInitialState = (input, select, date) => {
  const orderByGroup = input.concat(select, date);
  const orderBy = orderByGroup.reduce((obj, key) => Object.assign(obj, { [key]: 1 }), {});
  return orderBy;
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

const onSortData = (data, order, property) => {
  if (order === 1) {
    return data.sort((a, b) => {
      const textA = a[property].toUpperCase();
      const textB = b[property].toUpperCase();
      return textA < textB ? -1 : textA > textB ? 1 : 0;
    });
  } else {
    return data.sort((a, b) => {
      const textA = a[property].toUpperCase();
      const textB = b[property].toUpperCase();
      return textA < textB ? 1 : textA > textB ? -1 : 0;
    });
  }
};

export const filterUtils = {
  getFilterInitialState,
  getOptionTypes,
  getOrderInitialState,
  onSortData
};
