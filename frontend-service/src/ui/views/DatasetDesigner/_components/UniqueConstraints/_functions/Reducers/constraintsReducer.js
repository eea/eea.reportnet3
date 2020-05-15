export const constraintsReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'FILTERED_DATA':
      return { ...state, filteredData: payload.data };

    case 'IS_LOADING':
      return { ...state, isLoading: payload };
    case 'IS_DELETE_DIALOG_VISIBLE':
      return { ...state, isDeleteDialogVisible: payload.value };

    case 'ON_LOAD_CONSTRAINT_ID':
      return { ...state, fieldId: payload.value };

    case 'IS_DATA_UPDATED':
      return { ...state, isDataUpdated: payload.value };

    default:
      return state;
  }
};
