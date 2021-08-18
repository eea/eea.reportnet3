export const nationalSystemsFieldReducer = (state, { type, payload }) => {
  switch (type) {
    case 'typeName':
      return { ...state, ...payload };

    case 'HANDLE_DIALOGS':
      return { ...state, isDialogVisible: { ...state.isDialogVisible, [payload.dialog]: payload.value } };

    case 'ON_FILL_FIELD':
      return { ...state, field: { ...state.field, value: payload.value } };

    default:
      return state;
  }
};
