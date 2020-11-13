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
        selectedId: payload.recordId,
        selectedTableName: null,
        selectedTable: { ...state.selectedTable, tableName: null, recordId: payload.recordId, pamsId: payload.pamsId }
      };

    case 'ON_SELECT_TABLE':
      return {
        ...state,
        selectedTableName: payload.name,
        selectedTable: { ...state.selectedTable, tableName: payload.name }
      };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'SET_IS_ADDING_RECIRD':
      return { ...state, isAddingRecord: payload.value };

    default:
      return state;
  }
};
