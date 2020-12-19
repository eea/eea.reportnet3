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

    case 'SET_IS_ADDING_SINGLE_RECORD':
      return { ...state, isAddingSingleRecord: payload.value };

    case 'SET_IS_ADDING_GROUP_RECORD':
      return { ...state, isAddingGroupRecord: payload.value };

    case 'ON_REFRESH':
      return { ...state, isRefresh: payload.value };

    case 'ON_SELECT_SCHEMA_ID':
      return { ...state, selectedTable: { ...state.selectedTable, fieldSchemaId: payload.fieldSchemaId } };

    case 'GET_TABLE_SCHEMA_ID':
      return { ...state, selectedTableSchemaId: payload.tableSchemaId };

    case 'HAS_ERRORS':
      return { ...state, hasErrors: payload.value };
    case 'UPDATE_PAMS_RECORDS':
      const inmTableList = { ...state.tableList };
      Object.values(inmTableList).forEach(element => {
        element.forEach(pam => {
          if (pam.recordId === payload.recordId) {
            pam.id = payload.pamsId;
          }
        });
      });

      const inmPamsRecords = [...state.pamsRecords];
      inmPamsRecords.forEach(pamRecord => {
        if (pamRecord.recordId === payload.recordId) {
          pamRecord.fields.forEach(field => {
            if (field.fieldId === payload.fieldId) {
              field.value = payload.pamsId;
            }
          });
        }
      });
      console.log(inmPamsRecords);

      const inmSelectedTable = { ...state.selectedTable };
      inmSelectedTable.pamsId = payload.pamsId;

      return { ...state, pamsRecords: inmPamsRecords, tableList: inmTableList, selectedTable: inmSelectedTable };

    default:
      return state;
  }
};
