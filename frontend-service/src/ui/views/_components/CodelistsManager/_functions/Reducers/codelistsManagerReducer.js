import { cloneDeep } from 'lodash';

const sortCategories = (data, order) => {
  if (order === 1) {
    return data.sort((a, b) => {
      const textA = a.shortCode.toUpperCase();
      const textB = b.shortCode.toUpperCase();
      return textA < textB ? -1 : textA > textB ? 1 : 0;
    });
  } else {
    return data.sort((a, b) => {
      const textA = a.shortCode.toUpperCase();
      const textB = b.shortCode.toUpperCase();
      return textA < textB ? 1 : textA > textB ? -1 : 0;
    });
  }
};

export const codelistsManagerReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_CATEGORIES':
      return { ...state, categories: payload.categories };
    case 'SET_FILTER':
      return {
        ...state,
        filteredCategories: cloneDeep(payload.data),
        filter: payload.filter,
        isFiltered: payload.filter !== ''
      };
    case 'ORDER_CATEGORIES':
      return {
        ...state,
        filteredCategories: sortCategories([...state.filteredCategories], payload.order),
        categories: sortCategories([...state.categories], payload.order),
        order: -payload.order
      };
    default:
      return state;
  }
};
