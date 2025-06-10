import cloneDeep from 'lodash/cloneDeep';

export const tableManagementReducer = (state, { type, payload }) => {
  switch (type) {
    case 'DELETE_ROW':
      return { ...state, isDeletingRow: payload };

    case 'EDIT_SELECTED_RECORD':
      return { ...state, selectedRecord: payload };

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
      return { ...state, parentTablesWithData: payload };

    case 'SET_SELECTED_RECORD':
      return {
        ...state,
        initialSelectedRecord: cloneDeep(payload.selectedRecord),
        selectedRecord: payload.selectedRecord
      };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };
    case 'ON_CHANGE_PAGE':
      return {
        ...state,
        firstPageRecord: payload.first,
        recordsPerPage: payload.rowsPerPage
      };
    case 'SET_RECORDS':
      return {
        ...state,
        records: payload
      };
    case 'SET_RECORDS_PER_PAGE':
      return { ...state, recordsPerPage: payload.recordsPerPage };
    case 'SET_TOTAL':
      return { ...state, totalRecords: payload.totalRecords };
    case 'SET_FILTERED':
      return { ...state, totalFilteredRecords: payload.totalFilteredRecords };
    case 'SET_FIRST_PAGE_RECORD':
      return { ...state, firstPageRecord: payload.firstPageRecord };
    case 'SET_TABLE_DATA':
      return {
        ...state,
        records: payload.records,
        totalRecords: payload.totalRecords,
        totalFilteredRecords: payload.totalFilteredRecords
      };
    case 'SET_PAGINATION':
      return {
        ...state,
        firstPageRecord: payload.firstPageRecord,
        recordsPerPage: payload.recordsPerPage
      };
    default:
      return state;
  }
};
