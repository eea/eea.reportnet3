export const article13Reducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'ON_TOGGLE_VIEW':
      return { ...state, view: payload.view };

    case 'ON_LOAD_PAMS_DATA':
      return {
        ...state,
        pamsRecords: payload.records,
        tableList: { ...state.tableList, single: payload.single, group: payload.group }
      };

    case 'ON_UPDATE_DATA':
      return { ...state, isDataUpdated: payload.value };

    case 'ON_SELECT_RECORD':
      return {
        ...state,
        selectedTableName: null,
        selectedTable: { ...state.selectedTable, pamsId: payload.pamsId, recordId: payload.recordId, tableName: null }
      };

    case 'ON_SELECT_TABLE':
      return {
        ...state,
        selectedTableName: payload.name,
        selectedTable: { ...state.selectedTable, tableName: payload.name }
      };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'SET_IS_ADDING_RECORD':
      return { ...state, isAddingRecord: payload.value };

    case 'ON_REFRESH':
      return { ...state, isRefresh: payload.value };

    case 'ON_SELECT_SCHEMA_ID':
      return { ...state, selectedTable: { ...state.selectedTable, fieldSchemaId: payload.fieldSchemaId } };

    case 'GET_TABLE_SCHEMA_ID':
      return { ...state, selectedTableSchemaId: payload.tableSchemaId };

    case 'HAS_ERRORS':
      return { ...state, hasErrors: payload.value };

    default:
      return state;
  }
};
