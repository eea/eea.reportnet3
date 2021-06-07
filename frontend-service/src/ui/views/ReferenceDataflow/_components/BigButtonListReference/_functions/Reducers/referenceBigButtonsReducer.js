export const referenceBigButtonsReducer = (state, { type, payload }) => {
  switch (type) {
    case 'HANDLE_DIALOGS':
      return { ...state, dialogVisibility: { ...state.dialogVisibility, [payload.dialog]: payload.isVisible } };

    case 'GET_DELETE_INDEX':
      return { ...state, deleteIndex: payload.index };

    default:
      return state;
  }
};
