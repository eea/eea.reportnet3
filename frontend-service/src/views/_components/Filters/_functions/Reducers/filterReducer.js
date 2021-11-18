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
        checkboxes: payload.checkboxes,
        clearedFilters: payload.clearedFilters,
        filterBy: payload.filterBy,
        filtered: payload.filtered,
        filteredData: payload.filteredData,
        filteredSearched: payload.filteredSearched,
        labelAnimations: payload.labelAnimations,
        orderBy: payload.orderBy,
        property: '',
        searchBy: payload.searchBy,
        isOrdering: false
      };

    case 'FILTERED':
      return { ...state, filtered: payload.filteredStateValue };

    case 'FILTER_DATA':
      return {
        ...state,
        filterBy: { ...state.filterBy, [payload.filter]: payload.value },
        filteredData: payload.filteredData,
        isOrdering: false
      };

    case 'FILTERED_SEARCHED_STATE':
      return { ...state, filteredSearched: payload.filteredSearchedValue };

    case 'INITIAL_STATE':
      const getFilterBy = () => {
        if (state.previousState?.filtered) {
          return { ...state.previousState.filterBy, ...state.filterBy };
        }
        return payload.initialFilterBy;
      };

      return {
        ...state,
        checkboxes: payload.initialCheckboxes,
        data: payload.initialData,
        filterBy: getFilterBy(),
        filteredData: payload.initialFilteredData,
        labelAnimations: payload.initialLabelAnimations,
        orderBy: payload.initialOrderBy,
        previousState: { filtered: state.filtered, filterBy: state.filterBy },
        isOrdering: false
      };

    case 'ON_SEARCH_DATA':
      return {
        ...state,
        filteredData: payload.searchedValues,
        searchBy: payload.value,
        searched: payload.searched,
        isOrdering: false
      };

    case 'UPDATE_FILTER_BY':
      return {
        ...state,
        filterBy: payload.filterBy,
        previousState: { filtered: state.filtered, filterBy: payload.filterBy },
        isOrdering: false
      };

    case 'ON_CHECKBOX_FILTER':
      return {
        ...state,
        checkboxes: FiltersUtils.getCheckboxState(state.checkboxes, payload.property),
        property: payload.property,
        isOrdering: false
      };

    case 'ORDER_DATA':
      return {
        ...state,
        data: payload.sortedData,
        filteredData: payload.filteredSortedData,
        orderBy: { ...payload.resetOrder, [payload.property]: -payload.orderBy },
        isOrdering: true
      };

    case 'SET_CLEARED_FILTERS':
      return { ...state, clearedFilters: payload };

    case 'TOGGLE_MATCH_MODE':
      return { ...state, matchMode: payload };

    default:
      return state;
  }
};
