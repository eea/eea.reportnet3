export const nationalSystemsFieldReducer = (state, { type, payload }) => {
  switch (type) {
    case 'typeName':
      return { ...state, ...payload };

    case 'HANDLE_DIALOGS':
      return { ...state, isDialogVisible: { ...state.isDialogVisible, [payload.dialog]: payload.value } };

    default:
      return state;
  }
};
