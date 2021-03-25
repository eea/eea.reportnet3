export const nationalSystemsFieldReducer = (state, { type, payload }) => {
  switch (type) {
    case 'typeName':
      return { ...state, ...payload };

    case 'HANDLE_DIALOGS':
      return { ...state, isDialogVisible: { ...state.isDialogVisible, [payload.dialog]: payload.value } };

    case 'LOAD_SELECTED_VALID_EXTENSIONS':
      return { ...state, selectedValidExtensions: payload.data };

    case 'ON_FILL_FIELD':
      return { ...state, field: { ...state.field, value: payload.value } };

    default:
      return state;
  }
};
