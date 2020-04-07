export const reportingObligationReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'FILTER_DATA':
      return { ...state, filteredData: payload.filteredData };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'ON_LOAD_COUNTRIES':
      return { ...state, countries: payload.countries };

    case 'ON_LOAD_ISSUES':
      return { ...state, issues: payload.issues };

    case 'ON_LOAD_ORGANIZATIONS':
      return { ...state, organizations: payload.organizations };

    case 'ON_PAGINATE':
      return { ...state, pagination: payload.pagination };

    case 'ON_SELECT_OBL':
      return { ...state, oblChoosed: payload.oblChoosed };

    case 'ON_TOGGLE_VIEW':
      return { ...state, isTableView: payload.view };

    case 'SEARCHED_DATA':
      return { ...state, searchedData: payload.data };

    default:
      return state;
  }
};
