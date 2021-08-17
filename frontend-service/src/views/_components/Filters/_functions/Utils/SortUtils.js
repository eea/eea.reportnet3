import isBoolean from 'lodash/isBoolean';
import isNil from 'lodash/isNil';

const getOrderIcon = order => {
  if (order === 0) return 'sortAlt';
  else if (order === 1) return 'alphabeticOrderDown';
  else if (order === -1) return 'alphabeticOrderUp';
};

const getOrderInitialState = (input = [], select = [], date = [], dropDown = [], checkbox = []) => {
  const orderByGroup = input.concat(select, date, dropDown, checkbox);
  const orderByState = orderByGroup.reduce((obj, key) => Object.assign(obj, { [key]: 0 }), {});
  date.forEach(date => {
    orderByState[date] = 1;
  });
  return orderByState;
};

const onResetOrderData = (input = [], select = [], date = [], check = []) => {
  return input.concat(select, date, check).reduce((obj, key) => Object.assign(obj, { [key]: 0 }), {});
};

const onSortData = (data, order, property, sortCategory) => {
  return data.sort((a, b) => {
    if (!isNil(a[property]) && !isNil(b[property]) && !isBoolean(a[property]) && !isBoolean(b[property])) {
      const textA = a[property].toUpperCase();
      const textB = b[property].toUpperCase();

      if (!isNil(sortCategory) && a[sortCategory] !== b[sortCategory]) {
        return a[sortCategory] < b[sortCategory] ? -2 : 2;
      }

      const orderValue = order !== 1 ? -1 : 1;
      return textA < textB ? orderValue : textA > textB ? -orderValue : 0;
    }
    return 0;
  });
};

export const SortUtils = { getOrderIcon, getOrderInitialState, onResetOrderData, onSortData };
