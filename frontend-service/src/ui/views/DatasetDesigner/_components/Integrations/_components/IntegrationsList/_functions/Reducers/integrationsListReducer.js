export const integrationsListReducer = (state, { type, payload }) => {
  switch (type) {
    case 'FILTERED_DATA':
      return { ...state, filteredData: payload.data };

    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'IS_DATA_UPDATED':
      return { ...state, isDataUpdated: payload.value };

    case 'IS_DELETE_DIALOG_VISIBLE':
      return { ...state, isDeleteDialogVisible: payload.value };

    case 'IS_DELETING':
      return { ...state, isDeleting: payload };

    case 'IS_FILTERED':
      return { ...state, filtered: payload.value };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'ON_LOAD_INTEGRATION_ID':
      return { ...state, integrationId: payload.value };

    case 'SET_INTEGRATION_ID_TO_DELETE':
      return { ...state, integrationToDeleteId: payload.data };

    default:
      return state;
  }
};
