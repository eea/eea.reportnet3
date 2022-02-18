import isEmpty from 'lodash/isEmpty';

export const reportingObligationReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ON_LOAD_COUNTRIES':
      return { ...state, countries: payload.countries };

    case 'ON_LOAD_DATA':
      const getFilteredRecord = () => {
        if (payload.filteredRecords === payload.totalRecords && !isEmpty(payload.searchBy)) {
          return payload.filteredData.length;
        }

        return payload.filteredRecords;
      };

      const filteredRecords = getFilteredRecord();

      return {
        ...state,
        data: payload.data,
        filteredRecords,
        totalRecords: payload.totalRecords,
        isFiltered: filteredRecords !== payload.totalRecords
      };

    case 'ON_LOAD_ISSUES':
      return { ...state, issues: payload.issues };

    case 'ON_LOAD_ORGANIZATIONS':
      return { ...state, organizations: payload.organizations };

    case 'ON_PAGINATE':
      return { ...state, pagination: payload.pagination };

    case 'ON_SELECT_OBLIGATION':
      return { ...state, selectedObligation: payload.selectedObligation };

    case 'SET_LOADING':
      return { ...state, isLoading: payload.status };

    default:
      return state;
  }
};
