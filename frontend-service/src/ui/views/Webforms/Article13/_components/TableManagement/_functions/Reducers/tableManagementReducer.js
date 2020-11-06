export const tableManagementReducer = (state, { type, payload }) => {
  switch (type) {
    case 'EDIT_SELECTED_RECORD':
      const inmSelectedRecord = { ...state.selectedRecord };
      inmSelectedRecord[payload.property] = payload.value;
      return {
        ...state,
        selectedRecord: inmSelectedRecord
      };
    case 'INITIAL_LOAD':
      return { ...state, ...payload };
    case 'MANAGE_DIALOGS':
      return { ...state, isDialogVisible: { ...state.isDialogVisible, [payload.dialog]: payload.value } };
    case 'RESET_SELECTED_RECORD':
      return { ...state, selectedRecord: state.initialSelectedRecord };
    case 'SET_COLUMNS':
      return { ...state, tableColumns: payload };
    case 'SET_PARENT_TABLES_DATA':
      return {
        ...state,
        parentTablesWithData: payload
      };
    case 'SET_SELECTED_RECORD':
      return { ...state, initialSelectedRecord: payload.selectedRecord, selectedRecord: payload.selectedRecord };
    default:
      return state;
  }
};
