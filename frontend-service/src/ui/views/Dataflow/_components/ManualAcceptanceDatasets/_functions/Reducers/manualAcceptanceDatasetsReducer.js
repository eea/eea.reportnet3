export const manualAcceptanceDatasetsReducer = (state, { type, payload }) => {
  switch (type) {
    case 'FILTERED_DATA':
      return { ...state, filteredData: payload.data };

    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'IS_FILTERED':
      return { ...state, filtered: payload.value };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'MANAGE_DIALOGS':
      return { ...state, [payload.dialog]: payload.value, [payload.secondDialog]: payload.secondValue };

    case 'ON_ROW_CLICK':
      return { ...state, datasetToEdit: payload.data };

    case 'ON_UPDATED_DATA':
      return { ...state, isUpdatedData: payload.value };

    default:
      return state;
  }
};
