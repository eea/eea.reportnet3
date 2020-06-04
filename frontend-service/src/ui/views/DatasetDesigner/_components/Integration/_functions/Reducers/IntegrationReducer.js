export const IntegrationReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'IS_DELETE_DIALOG_VISIBLE':
      return { ...state, isDeleteDialogVisible: payload.value };

    case 'IS_LOADING':
      return { ...state, isLoading: payload };

    case 'ON_LOAD_INTEGRATION_ID':
      return { ...state, integrationId: payload.value };

    default:
      return state;
  }
};
