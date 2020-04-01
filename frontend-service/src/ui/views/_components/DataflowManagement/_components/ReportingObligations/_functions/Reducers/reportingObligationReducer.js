export const reportingObligationReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'ON_SELECT_OBL':
      return { ...state, oblChoosed: payload.oblChoosed };

    case 'ON_TOGGLE_VIEW':
      return { ...state, isTableView: payload.view };

    case 'FILTER_DATA':
      return { ...state, filteredData: payload.filteredData };

    case 'SEARCHED_DATA':
      return { ...state, searchedData: payload.data };

    default:
      return state;
  }
};
