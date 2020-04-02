import isNil from 'lodash/isNil';

const getOrderIcon = order => {
  if (order === 0) return 'sortAlt';
  else if (order === 1) return 'alphabeticOrderDown';
  else if (order === -1) return 'alphabeticOrderUp';
};

const getOrderInitialState = (input = [], select = [], date = []) => {
  const orderByGroup = input.concat(select, date);
  const orderByState = orderByGroup.reduce((obj, key) => Object.assign(obj, { [key]: 0 }), {});
  date.forEach(date => {
    orderByState[date] = 1;
  });
  return orderByState;
};

const onResetOrderData = (input = [], select = [], date = []) => {
  return input.concat(select, date).reduce((obj, key) => Object.assign(obj, { [key]: 0 }), {});
};

const onSortData = (data, order, property) => {
  if (order !== 1) {
    return data.sort((a, b) => {
      if (!isNil(a[property]) && !isNil(b[property])) {
        const textA = a[property].toUpperCase();
        const textB = b[property].toUpperCase();
        return textA < textB ? -1 : textA > textB ? 1 : 0;
      }
    });
  } else {
    return data.sort((a, b) => {
      if (!isNil(a[property]) && !isNil(b[property])) {
        const textA = a[property].toUpperCase();
        const textB = b[property].toUpperCase();
        return textA < textB ? 1 : textA > textB ? -1 : 0;
      }
    });
  }
};

export const SortUtils = { getOrderIcon, getOrderInitialState, onResetOrderData, onSortData };
