export const tableManagementReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'GET_DELETE_ID':
      return { ...state, deleteId: payload.deleteId };

    case 'MANAGE_DIALOGS':
      return { ...state, isDialogVisible: { ...state.isDialogVisible, [payload.dialog]: payload.value } };
    case 'SET_COLUMNS':
      return { ...state, tableColumns: payload };
    case 'SET_PARENT_TABLES_DATA':
      return {
        ...state,
        parentTablesWithData: payload
      };
    default:
      return state;
  }
};
