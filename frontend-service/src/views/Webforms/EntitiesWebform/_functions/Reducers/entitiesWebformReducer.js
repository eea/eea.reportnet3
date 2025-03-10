export const entitiesWebformReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'MANAGE_DIALOGS':
      return {
        ...state,
        [payload.dialog]: payload.value,
        rootPkInput: payload.rootPkInput
      };

    case 'ON_ADD_ENTITY_INPUT_CHANGE':
      return { ...state, rootPkInput: payload.rootPkInput };

    case 'ON_TOGGLE_VIEW':
      return { ...state, view: payload.view };

    case 'ON_LOAD_ENTITIES_DATA':
      return {
        ...state,
        entitiesRecords: payload.records,
        tableList: { ...state.tableList, single: payload.single },
        isDataUpdated: true
      };

    case 'ON_UPDATE_DATA':
      return { ...state, isDataUpdated: payload.value };

    case 'ON_SELECT_RECORD':
      return {
        ...state,
        selectedTableName: null,
        selectedTable: {
          ...state.selectedTable,
          rootTableId: payload.rootTableId,
          recordId: payload.recordId,
          tableName: null
        }
      };

    case 'ON_SELECT_TABLE':
      return {
        ...state,
        selectedTableName: payload.name,
        selectedTable: { ...state.selectedTable, tableName: payload.name }
      };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'SET_IS_ADDING_ENTITY_RECORD':
      return { ...state, isAddingEntityRecord: payload.value, isAddEntityIdDialogVisible: payload.value };

    case 'ON_REFRESH':
      return { ...state, isRefresh: payload.value };

    case 'ON_SELECT_SCHEMA_ID':
      return { ...state, selectedTable: { ...state.selectedTable, fieldSchemaId: payload.fieldSchemaId } };

    case 'GET_TABLE_SCHEMA_ID':
      return { ...state, selectedTableSchemaId: payload.tableSchemaId };

    case 'HAS_ERRORS':
      return { ...state, hasErrors: payload.value };

    case 'UPDATE_ENTITIES_RECORDS':
      const inmTableList = { ...state.tableList };
      Object.values(inmTableList).forEach(element => {
        element.forEach(entity => {
          if (entity.recordId === payload.recordId) {
            if (!payload.isEntityTitle) {
              entity.id = payload.entitiesValue;
            } else {
              entity.title = payload.entitiesValue;
            }
          }
        });
      });
      inmTableList.single.sort((a, b) => a.id - b.id);

      const inmEntitiesRecords = [...state.entitiesRecords];
      inmEntitiesRecords.forEach(entityRecord => {
        if (entityRecord.recordId === payload.recordId) {
          entityRecord.fields.forEach(field => {
            if (field.fieldId === payload.fieldId) {
              field.value = payload.entitiesValue;
            }
          });
        }
      });

      const inmSelectedTable = { ...state.selectedTable };
      if (!payload.isEntityTitle) {
        inmSelectedTable.rootTableId = payload.entitiesValue;
      }

      return {
        ...state,
        entitiesRecords: inmEntitiesRecords,
        tableList: inmTableList,
        // selectedTable: inmSelectedTable,
        isDataUpdated: payload.dataUpdated
      };

    case 'UPDATE_DATA':
      return { ...state, data: payload.data };

    default:
      return state;
  }
};
