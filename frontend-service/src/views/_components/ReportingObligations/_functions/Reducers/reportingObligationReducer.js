export const reportingObligationReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'IS_FILTERED_SEARCHED':
      return { ...state, filteredSearched: payload.value };

    case 'IS_FILTERED':
      return { ...state, isFiltered: payload.value };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'IS_SEARCHED':
      return { ...state, isSearched: payload.value };

    case 'ON_LOAD_COUNTRIES':
      return { ...state, countries: payload.countries };

    case 'ON_LOAD_ISSUES':
      return { ...state, issues: payload.issues };

    case 'ON_LOAD_ORGANIZATIONS':
      return { ...state, organizations: payload.organizations };

    case 'ON_PAGINATE':
      return { ...state, pagination: payload.pagination };

    case 'ON_SELECT_OBL':
      return { ...state, selectedObligation: payload.selectedObligation };

    case 'SEARCHED_DATA':
      return { ...state, searchedData: payload.data };

    default:
      return state;
  }
};
