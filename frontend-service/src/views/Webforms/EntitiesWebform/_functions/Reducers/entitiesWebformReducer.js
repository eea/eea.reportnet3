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
        entitiesList: payload.list,
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
      return { ...state, isAddingEntityRecord: payload.value };

    case 'SET_IS_ADD_ENTITY_ID_DIALOG_VISIBLE':
      return { ...state, isAddEntityIdDialogVisible: payload.value };

    case 'SET_IS_VIEW_MODE':
      return { ...state, isViewMode: payload.value };

    case 'SET_IS_UPDATING_FIELD':
      return {
        ...state,
        updatingField: {
          field: payload.field,
          isUpdating: payload.value
        }
      };

    case 'ON_REFRESH':
      return { ...state, isRefresh: payload.value };

    case 'ON_SELECT_SCHEMA_ID':
      return { ...state, selectedTable: { ...state.selectedTable, fieldSchemaId: payload.fieldSchemaId } };

    case 'GET_TABLE_SCHEMA_ID':
      return { ...state, selectedTableSchemaId: payload.tableSchemaId };

    case 'HAS_ERRORS':
      return { ...state, hasErrors: payload.value };

    case 'UPDATE_DATA':
      return { ...state, data: payload.data };

    default:
      return state;
  }
};
