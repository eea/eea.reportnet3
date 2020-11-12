import cloneDeep from 'lodash/cloneDeep';

export const tableManagementReducer = (state, { type, payload }) => {
  switch (type) {
    case 'EDIT_SELECTED_RECORD':
      return {
        ...state,
        selectedRecord: payload
      };
    case 'INITIAL_LOAD':
      return { ...state, ...payload };
    case 'MANAGE_DIALOGS':
      return { ...state, isDialogVisible: { ...state.isDialogVisible, [payload.dialog]: payload.value } };
    case 'ON_SAVE_RECORD':
      return { ...state, isSaving: false, isDialogVisible: false };
    case 'RESET_SELECTED_RECORD':
      return { ...state, records: payload.records, isDialogVisible: false };
    case 'SET_COLUMNS':
      return { ...state, tableColumns: payload };
    case 'SET_IS_SAVING':
      return { ...state, isSaving: payload };
    case 'SET_PARENT_TABLES_DATA':
      return {
        ...state,
        parentTablesWithData: payload
      };
    case 'SET_SELECTED_RECORD':
      return {
        ...state,
        initialSelectedRecord: cloneDeep(payload.selectedRecord),
        selectedRecord: payload.selectedRecord
      };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    default:
      return state;
  }
};
