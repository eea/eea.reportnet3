export const tableManagementReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'GET_DELETE_ID':
      return { ...state, deleteId: payload.deleteId };

    case 'MANAGE_DIALOGS':
      return { ...state, isDialogVisible: { ...state.isDialogVisible, [payload.dialog]: payload.value } };

    default:
      return state;
  }
};
