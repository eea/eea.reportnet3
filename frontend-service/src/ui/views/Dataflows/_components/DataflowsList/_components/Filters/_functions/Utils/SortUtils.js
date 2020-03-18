const getOrderInitialState = (input = [], select = [], date = []) => {
  const orderByGroup = input.concat(select, date);
  return orderByGroup.reduce((obj, key) => Object.assign(obj, { [key]: 1 }), {});
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

export const SortUtils = { getOrderInitialState, onSortData };
