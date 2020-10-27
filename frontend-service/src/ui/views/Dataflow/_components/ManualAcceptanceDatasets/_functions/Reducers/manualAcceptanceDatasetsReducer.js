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

    case 'ON_CLOSE_EDIT_DIALOG':
      return { ...state, isManageDatasetDialogVisible: payload.value };

    case 'ON_EDIT_DATASET':
      return { ...state, datasetToEdit: payload.datasetToEdit, isManageDatasetDialogVisible: payload.value };

    case 'ON_UPDATED_DATA':
      return { ...state, isUpdatedData: payload.value };

    default:
      return state;
  }
};
