import { cloneDeep } from 'lodash';

import { CodelistsManagerUtils } from '../Utils/CodelistsManagerUtils';

const changeExpanded = (categories, categoriesExpandStatus, categoryId, expanded) => {
  const inmCategoriesExpandStatus = categoriesExpandStatus;
  const id = CodelistsManagerUtils.getCategoryById(categories, categoryId);
  inmCategoriesExpandStatus[id].expanded = expanded;
  return inmCategoriesExpandStatus;
};

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
    case 'COLLAPSE_ALL':
      return { ...state, expandAll: false, collapseAll: true, toggleExpandCollapseAll: 0 };
    case 'EXPAND_ALL':
      return { ...state, expandAll: true, collapseAll: false, toggleExpandCollapseAll: 1 };
    case 'ORDER_CATEGORIES':
      return {
        ...state,
        filteredCategories: sortCategories([...state.filteredCategories], payload.order),
        categories: sortCategories([...state.categories], payload.order),
        order: -payload.order
      };
    case 'SET_CATEGORIES':
      return { ...state, categories: payload.categories };
    case 'SET_EXPANDED_STATUS':
      return {
        ...state,
        categoriesExpandStatus: changeExpanded(
          [...state.categories],
          [...state.categoriesExpandStatus],
          payload.categoryId,
          payload.expanded
        )
      };
    case 'SET_INITIAL_EXPANDED_STATUS':
      return {
        ...state,
        categoriesExpandStatus: [...state.categories].map(category => {
          return { categoryId: category.id, expanded: false };
        })
      };
    case 'SET_FILTER':
      return {
        ...state,
        filteredCategories: cloneDeep(payload.data),
        filter: payload.filter,
        isFiltered: payload.filter !== ''
      };
    default:
      return state;
  }
};
