import { FiltersUtils } from '../Utils/FiltersUtils';

export const filterReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ANIMATE_LABEL':
      return {
        ...state,
        labelAnimations: { ...state.labelAnimations, [payload.animatedProperty]: payload.isAnimated }
      };

    case 'CLEAR_ALL':
      return {
        ...state,
        filterBy: payload.filterBy,
        filteredData: payload.filteredData,
        labelAnimations: payload.labelAnimations,
        orderBy: payload.orderBy,
        searchBy: payload.searchBy,
        checkboxes: payload.checkboxes,
        filtered: payload.filtered,
        filteredSearched: payload.filteredSearched,
        property: '',
        clearedFilters: payload.clearedFilters
      };

    case 'FILTERED':
      return {
        ...state,
        filtered: payload.filteredStateValue
      };

    case 'FILTER_DATA':
      return {
        ...state,
        filterBy: { ...state.filterBy, [payload.filter]: payload.value },
        filteredData: payload.filteredData
      };

    case 'FILTERED_SEARCHED_STATE':
      return {
        ...state,
        filteredSearched: payload.filteredSearchedValue
      };

    case 'INITIAL_STATE':
      return {
        ...state,
        data: payload.initialData,
        filterBy: payload.initialFilterBy,
        filteredData: payload.initialFilteredData,
        labelAnimations: payload.initialLabelAnimations,
        orderBy: payload.initialOrderBy,
        checkboxes: payload.initialCheckboxes
      };

    case 'ON_SEARCH_DATA':
      return {
        ...state,
        filteredData: payload.searchedValues,
        searchBy: payload.value,
        searched: payload.searched
      };

    case 'ON_CHECKBOX_FILTER':
      return {
        ...state,
        checkboxes: FiltersUtils.getCheckboxState(state.checkboxes, payload.property, payload.value),
        property: payload.property
      };

    case 'ORDER_DATA':
      return {
        ...state,
        data: payload.sortedData,
        filteredData: payload.filteredSortedData,
        orderBy: { ...payload.resetOrder, [payload.property]: -payload.orderBy }
      };

    case 'SET_CLEARED_FILTERS':
      return {
        ...state,
        clearedFilters: payload
      };

    case 'TOGGLE_MATCH_MODE':
      return { ...state, matchMode: payload };

    default:
      return state;
  }
};
