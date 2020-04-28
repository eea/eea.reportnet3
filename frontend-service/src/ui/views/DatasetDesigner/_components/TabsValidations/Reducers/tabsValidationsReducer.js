export const tabsValidationsReducer = (state, { type, payload }) => {
  switch (type) {
    case 'FILTER_DATA':
      return { ...state, filteredData: payload.data };

    case 'IS_DATA_UPDATED':
      return { ...state, isDataUpdated: payload.value };

    case 'IS_DELETE_DIALOG_VISIBLE':
      return { ...state, isDeleteDialogVisible: payload.value };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'ON_LOAD_VALIDATION_ID':
      return { ...state, validationId: payload.value };

    case 'ON_LOAD_VALIDATION_LIST':
      return { ...state, validationList: payload.validationsServiceList };

    case 'SEARCHED_DATA':
      return { ...state, searchedData: payload.data };
  }
};
