export const euDatasetReducer = (state, { type, payload }) => {
  switch (type) {
    case 'GET_DATA_SCHEMA':
      return {
        ...state,
        datasetSchemaAllTables: payload.allTables,
        datasetSchemaName: payload.schemaName,
        levelErrorTypes: payload.errorTypes
      };

    case 'GET_METADATA':
      return { ...state, metaData: payload.metadata };

    case 'HANDLE_DIALOGS':
      return { ...state, isDialogVisible: { ...state.isDialogVisible, [payload.dialog]: payload.value } };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'IS_WEB_FORM_MMR':
      return { ...state, isInputSwitchChecked: payload.value, isWebFormMMR: payload.value };

    case 'ON_LOAD_TABLE_DATA':
      return { ...state, datasetHasData: payload.hasData };

    case 'ON_LOAD_DATASET_SCHEMA':
      return {
        ...state,
        datasetHasErrors: payload.datasetErrors,
        datasetName: payload.datasetName,
        tableSchema: payload.tableSchema,
        tableSchemaColumns: payload.tableSchemaColumns,
        tableSchemaId: payload.schemaId,
        tableSchemaNames: payload.tableSchemaNames
      };

    case 'ON_TAB_CHANGE':
      return { ...state, dataViewerOptions: { ...state.dataViewerOptions, activeIndex: payload.activeIndex } };

    default:
      return state;
  }
};
